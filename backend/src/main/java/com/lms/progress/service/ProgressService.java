package com.lms.progress.service;

import com.lms.certificate.service.CertificateService;
import com.lms.config.security.UserPrincipal;
import com.lms.course.service.CourseService;
import com.lms.lesson.entity.ContentStatus;
import com.lms.lesson.entity.Lesson;
import com.lms.lesson.repository.LessonRepository;
import com.lms.progress.dto.CourseProgressSummaryResponse;
import com.lms.progress.entity.CourseCompletion;
import com.lms.progress.entity.LessonProgress;
import com.lms.progress.entity.ProgressStatus;
import com.lms.progress.repository.CourseCompletionRepository;
import com.lms.progress.repository.LessonProgressRepository;
import com.lms.shared.exception.BusinessRuleViolationException;
import com.lms.shared.exception.ResourceNotFoundException;
import com.lms.shared.response.PageResponse;
import com.lms.subscription.entity.Subscription;
import com.lms.subscription.repository.SubscriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Ref: SRS Chapter 12 - Student Progress Tracking. Manual, per-lesson completion only in Version 1. */
@Service
public class ProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final CourseCompletionRepository courseCompletionRepository;
    private final LessonRepository lessonRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CourseService courseService;
    private final CertificateService certificateService;

    public ProgressService(
            LessonProgressRepository lessonProgressRepository,
            CourseCompletionRepository courseCompletionRepository,
            LessonRepository lessonRepository,
            SubscriptionRepository subscriptionRepository,
            CourseService courseService,
            CertificateService certificateService
    ) {
        this.lessonProgressRepository = lessonProgressRepository;
        this.courseCompletionRepository = courseCompletionRepository;
        this.lessonRepository = lessonRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.courseService = courseService;
        this.certificateService = certificateService;
    }

    @Transactional
    public CourseProgressSummaryResponse markLessonComplete(UUID lessonId, UUID studentId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found."));
        if (lesson.getStatus() != ContentStatus.PUBLISHED) {
            throw new BusinessRuleViolationException("Only published lessons can be marked complete.");
        }

        // Ref: SRS 12.4 - "Idempotent - completion is recorded once and
        // reopening does not reset it."
        LessonProgress progress = lessonProgressRepository.findByStudentIdAndLessonId(studentId, lessonId)
                .orElseGet(() -> {
                    LessonProgress created = new LessonProgress();
                    created.setStudentId(studentId);
                    created.setLessonId(lessonId);
                    created.setStartedAt(OffsetDateTime.now());
                    return created;
                });
        if (progress.getStatus() != ProgressStatus.COMPLETED) {
            progress.setStatus(ProgressStatus.COMPLETED);
            progress.setCompletedAt(OffsetDateTime.now());
            lessonProgressRepository.save(progress);
        }

        return computeAndMaybeCompleteCourse(studentId, lesson.getCourseId(), lessonId);
    }

    @Transactional(readOnly = true)
    public CourseProgressSummaryResponse getOwnProgress(UUID courseId, UUID studentId) {
        return computeProgress(studentId, courseId, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseProgressSummaryResponse> listCourseProgress(UUID courseId, UserPrincipal principal, Pageable pageable) {
        // Ref: SRS 12.7 - "Assigned instructors only, for their own courses."
        courseService.assertAdminOrAssignedInstructor(courseId, principal);

        Page<Subscription> enrolled = subscriptionRepository.findByCourseId(courseId, pageable);
        return PageResponse.from(enrolled, sub -> computeProgress(sub.getStudentId(), courseId, null));
    }

    /** Recomputes progress and, if this completion finishes the course, records it and issues a certificate. */
    private CourseProgressSummaryResponse computeAndMaybeCompleteCourse(UUID studentId, UUID courseId, UUID lastCompletedLessonId) {
        CourseProgressSummaryResponse progress = computeProgress(studentId, courseId, lastCompletedLessonId);

        if ("COMPLETED".equals(progress.completionStatus()) && courseCompletionRepository.findByStudentIdAndCourseId(studentId, courseId).isEmpty()) {
            CourseCompletion completion = new CourseCompletion();
            completion.setStudentId(studentId);
            completion.setCourseId(courseId);
            completion.setCompletedAt(OffsetDateTime.now());
            courseCompletionRepository.save(completion);

            certificateService.issueIfEligible(studentId, courseId, completion.getCompletedAt());

            return new CourseProgressSummaryResponse(
                    progress.studentId(), progress.courseId(), progress.progressPercentage(),
                    progress.completedLessons(), progress.totalPublishedLessons(), progress.lastCompletedLessonId(),
                    progress.completionStatus(), completion.getCompletedAt());
        }
        return progress;
    }

    private CourseProgressSummaryResponse computeProgress(UUID studentId, UUID courseId, UUID lastCompletedLessonId) {
        List<Lesson> publishedLessons = lessonRepository.findByCourseIdAndStatus(courseId, ContentStatus.PUBLISHED);
        List<UUID> publishedLessonIds = publishedLessons.stream().map(Lesson::getId).toList();

        int total = publishedLessonIds.size();
        int completed = publishedLessonIds.isEmpty() ? 0
                : (int) lessonProgressRepository.countByStudentIdAndStatusAndLessonIdIn(studentId, ProgressStatus.COMPLETED, publishedLessonIds);
        double percentage = total == 0 ? 0.0 : Math.round((completed * 10000.0) / total) / 100.0;
        boolean isComplete = total > 0 && completed >= total;

        OffsetDateTime completedAt = courseCompletionRepository.findByStudentIdAndCourseId(studentId, courseId)
                .map(CourseCompletion::getCompletedAt)
                .orElse(null);

        return new CourseProgressSummaryResponse(
                studentId, courseId, percentage, completed, total, lastCompletedLessonId,
                isComplete ? "COMPLETED" : "IN_PROGRESS", completedAt);
    }
}
