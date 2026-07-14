package com.lms.liveclass.service;

import com.lms.config.security.UserPrincipal;
import com.lms.course.service.CourseService;
import com.lms.liveclass.dto.CreateLiveClassRequest;
import com.lms.liveclass.dto.JoinLiveClassResponse;
import com.lms.liveclass.dto.LiveClassSummaryResponse;
import com.lms.liveclass.entity.LiveClass;
import com.lms.liveclass.entity.LiveClassAttendance;
import com.lms.liveclass.entity.LiveClassStatus;
import com.lms.liveclass.repository.LiveClassAttendanceRepository;
import com.lms.liveclass.repository.LiveClassRepository;
import com.lms.liveclass.repository.LiveClassSpecifications;
import com.lms.shared.exception.ForbiddenException;
import com.lms.shared.exception.ResourceNotFoundException;
import com.lms.shared.response.PageResponse;
import com.lms.subscription.service.SubscriptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/** Ref: SRS Chapter 11 - Live Classes. */
@Service
public class LiveClassService {

    private final LiveClassRepository liveClassRepository;
    private final LiveClassAttendanceRepository attendanceRepository;
    private final CourseService courseService;
    private final SubscriptionService subscriptionService;

    public LiveClassService(
            LiveClassRepository liveClassRepository,
            LiveClassAttendanceRepository attendanceRepository,
            CourseService courseService,
            SubscriptionService subscriptionService
    ) {
        this.liveClassRepository = liveClassRepository;
        this.attendanceRepository = attendanceRepository;
        this.courseService = courseService;
        this.subscriptionService = subscriptionService;
    }

    @Transactional(readOnly = true)
    public PageResponse<LiveClassSummaryResponse> listByCourse(UUID courseId, UserPrincipal principal, Pageable pageable) {
        boolean fullAccess = courseService.isAdminOrAssignedInstructor(courseId, principal);
        if (!fullAccess) {
            // Ref: SRS 11.8 - "Students see this only if they have an active subscription."
            if (!subscriptionService.hasActiveSubscription(principal.getUserId(), courseId)) {
                throw new ForbiddenException("No active subscription to this course.");
            }
        }
        Page<LiveClass> page = liveClassRepository.findByCourseId(courseId, pageable);
        return PageResponse.from(page, lc -> toSummary(lc, true));
    }

    @Transactional
    public LiveClassSummaryResponse createLiveClass(UUID courseId, CreateLiveClassRequest request, UserPrincipal principal) {
        courseService.assertAdminOrAssignedInstructor(courseId, principal);
        LiveClass liveClass = new LiveClass();
        liveClass.setCourseId(courseId);
        applyRequest(liveClass, request);
        // Plain save (not saveAndFlush) is fine here: unlike Order,
        // LiveClassSummaryResponse doesn't expose createdAt/updatedAt, so
        // there's no @Generated value this response depends on reading back.
        return toSummary(liveClassRepository.save(liveClass), true);
    }

    @Transactional(readOnly = true)
    public PageResponse<LiveClassSummaryResponse> searchLiveClasses(UUID courseId, LiveClassStatus status, LocalDate date, Pageable pageable) {
        Specification<LiveClass> spec = Specification.where(LiveClassSpecifications.hasCourse(courseId))
                .and(LiveClassSpecifications.hasStatus(status))
                .and(LiveClassSpecifications.onDate(date));
        return PageResponse.from(liveClassRepository.findAll(spec, pageable), lc -> toSummary(lc, true));
    }

    @Transactional(readOnly = true)
    public LiveClassSummaryResponse getLiveClassDetail(UUID liveClassId, UserPrincipal principal) {
        LiveClass liveClass = findLiveClassOrThrow(liveClassId);
        boolean showMeetingUrl = courseService.isAdminOrAssignedInstructor(liveClass.getCourseId(), principal)
                || subscriptionService.hasActiveSubscription(principal.getUserId(), liveClass.getCourseId());
        return toSummary(liveClass, showMeetingUrl);
    }

    @Transactional
    public void updateLiveClass(UUID liveClassId, CreateLiveClassRequest request, UserPrincipal principal) {
        LiveClass liveClass = findLiveClassOrThrow(liveClassId);
        courseService.assertAdminOrAssignedInstructor(liveClass.getCourseId(), principal);
        applyRequest(liveClass, request);
        liveClassRepository.save(liveClass);
    }

    @Transactional
    public void cancelLiveClass(UUID liveClassId, UserPrincipal principal) {
        LiveClass liveClass = findLiveClassOrThrow(liveClassId);
        courseService.assertAdminOrAssignedInstructor(liveClass.getCourseId(), principal);
        liveClass.setStatus(LiveClassStatus.CANCELLED);
        liveClassRepository.save(liveClass);
    }

    @Transactional
    public JoinLiveClassResponse joinLiveClass(UUID liveClassId, UserPrincipal principal) {
        LiveClass liveClass = findLiveClassOrThrow(liveClassId);
        // Ref: SRS 11.8, 11.9, 11.10 - "Requires an active subscription and a SCHEDULED live class."
        if (liveClass.getStatus() != LiveClassStatus.SCHEDULED
                || !subscriptionService.hasActiveSubscription(principal.getUserId(), liveClass.getCourseId())) {
            throw new ForbiddenException("No active subscription, or live class not SCHEDULED.");
        }

        // Ref: SRS 11.9 - rejoining an already-joined class is idempotent, not an error.
        attendanceRepository.findByLiveClassIdAndStudentId(liveClassId, principal.getUserId())
                .orElseGet(() -> {
                    LiveClassAttendance attendance = new LiveClassAttendance();
                    attendance.setLiveClassId(liveClassId);
                    attendance.setStudentId(principal.getUserId());
                    return attendanceRepository.save(attendance);
                });

        return new JoinLiveClassResponse(liveClass.getMeetingUrl(), liveClass.getMeetingPassword());
    }

    @Transactional
    public void addRecordingUrl(UUID liveClassId, String recordingUrl, UserPrincipal principal) {
        LiveClass liveClass = findLiveClassOrThrow(liveClassId);
        courseService.assertAdminOrAssignedInstructor(liveClass.getCourseId(), principal);
        liveClass.setRecordingUrl(recordingUrl);
        liveClassRepository.save(liveClass);
    }

    private void applyRequest(LiveClass liveClass, CreateLiveClassRequest request) {
        liveClass.setTitle(request.title());
        liveClass.setDescription(request.description());
        liveClass.setScheduledDate(request.scheduledDate());
        liveClass.setScheduledTime(request.scheduledTime());
        liveClass.setMeetingUrl(request.meetingUrl());
        liveClass.setMeetingPassword(request.meetingPassword());
    }

    private LiveClassSummaryResponse toSummary(LiveClass liveClass, boolean showMeetingUrl) {
        return new LiveClassSummaryResponse(
                liveClass.getId(),
                liveClass.getCourseId(),
                liveClass.getTitle(),
                liveClass.getDescription(),
                liveClass.getScheduledDate(),
                liveClass.getScheduledTime(),
                showMeetingUrl ? liveClass.getMeetingUrl() : null,
                liveClass.getRecordingUrl(),
                liveClass.getStatus().name()
        );
    }

    private LiveClass findLiveClassOrThrow(UUID liveClassId) {
        return liveClassRepository.findById(liveClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Live class not found."));
    }
}
