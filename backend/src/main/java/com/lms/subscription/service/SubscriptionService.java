package com.lms.subscription.service;

import com.lms.config.security.UserPrincipal;
import com.lms.course.service.CourseService;
import com.lms.shared.exception.BadRequestException;
import com.lms.shared.exception.ForbiddenException;
import com.lms.shared.exception.ResourceNotFoundException;
import com.lms.shared.response.PageResponse;
import com.lms.subscription.dto.AdminUpdateSubscriptionRequest;
import com.lms.subscription.dto.SubscriptionSummaryResponse;
import com.lms.subscription.entity.DurationUnit;
import com.lms.subscription.entity.Subscription;
import com.lms.subscription.entity.SubscriptionStatus;
import com.lms.subscription.repository.SubscriptionRepository;
import com.lms.subscription.repository.SubscriptionSpecifications;
import com.lms.user.entity.User;
import com.lms.user.repository.UserRepository;
import com.lms.user.repository.UserSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Ref: SRS Chapter 9 - Subscription Plans & Student Enrollments (subscriptions). */
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final CourseService courseService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository, CourseService courseService) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.courseService = courseService;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionSummaryResponse> getOwnSubscriptions(UUID studentId) {
        return subscriptionRepository.findByStudentId(studentId).stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<SubscriptionSummaryResponse> searchSubscriptions(
            String studentName, UUID courseId, SubscriptionStatus status, Pageable pageable
    ) {
        Specification<Subscription> spec = Specification.where(SubscriptionSpecifications.hasCourse(courseId))
                .and(SubscriptionSpecifications.hasStatus(status));

        if (studentName != null && !studentName.isBlank()) {
            // UserSpecifications.search() also matches email/mobile, a
            // slightly broader net than "studentName" alone - reused rather
            // than duplicating a near-identical query for this one filter.
            List<UUID> matchingStudentIds = userRepository.findAll(UserSpecifications.search(studentName)).stream()
                    .map(User::getId).toList();
            spec = spec.and(SubscriptionSpecifications.studentIdIn(matchingStudentIds));
        }

        Page<Subscription> page = subscriptionRepository.findAll(spec, pageable);
        return PageResponse.from(page, this::toSummary);
    }

    @Transactional(readOnly = true)
    public SubscriptionSummaryResponse getSubscriptionDetail(UUID subscriptionId, UserPrincipal principal) {
        Subscription subscription = findSubscriptionOrThrow(subscriptionId);
        // Ref: SRS 9.13 - "Administrator, or the owning student."
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR"));
        if (!isAdmin && !subscription.getStudentId().equals(principal.getUserId())) {
            throw new ForbiddenException("You do not have access to this subscription.");
        }
        return toSummary(subscription);
    }

    @Transactional
    public void adminUpdateSubscription(UUID subscriptionId, AdminUpdateSubscriptionRequest request) {
        Subscription subscription = findSubscriptionOrThrow(subscriptionId);
        if (request.status() != null) {
            subscription.setStatus(parseStatus(request.status()));
        }
        if (request.endDate() != null) {
            subscription.setEndDate(request.endDate());
        }
        subscriptionRepository.save(subscription);
    }

    /** Ref: SRS 7.14 - reused by LessonService for lesson-access gating. */
    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(UUID studentId, UUID courseId) {
        return subscriptionRepository.existsByStudentIdAndCourseIdAndStatusAndEndDateGreaterThanEqual(
                studentId, courseId, SubscriptionStatus.ACTIVE, LocalDate.now());
    }

    /**
     * Ref: SRS 9.11, 10.12 - called by Ch.10's webhook handler on a
     * successful payment. Exactly one (evolving) row per student+course:
     * a first purchase creates it, a renewal extends end_date rather than
     * creating a second row (the DB's UNIQUE(student_id, course_id) makes
     * a second row impossible anyway).
     */
    @Transactional
    public void activateOrRenew(UUID studentId, UUID courseId, UUID planId, int duration, DurationUnit durationUnit) {
        Optional<Subscription> existing = subscriptionRepository
                .findByStudentIdAndCourseId(studentId, courseId);

        LocalDate today = LocalDate.now();
        if (existing.isPresent()) {
            Subscription subscription = existing.get();
            LocalDate extendFrom = subscription.getEndDate().isBefore(today) ? today : subscription.getEndDate();
            subscription.setSubscriptionPlanId(planId);
            subscription.setEndDate(addDuration(extendFrom, duration, durationUnit));
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setPurchaseDate(OffsetDateTime.now());
            subscriptionRepository.save(subscription);
        } else {
            Subscription subscription = new Subscription();
            subscription.setStudentId(studentId);
            subscription.setCourseId(courseId);
            subscription.setSubscriptionPlanId(planId);
            subscription.setStartDate(today);
            subscription.setEndDate(addDuration(today, duration, durationUnit));
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setPurchaseDate(OffsetDateTime.now());
            subscriptionRepository.save(subscription);
        }
    }

    private LocalDate addDuration(LocalDate from, int duration, DurationUnit unit) {
        return switch (unit) {
            case DAY -> from.plusDays(duration);
            case MONTH -> from.plusMonths(duration);
            case YEAR -> from.plusYears(duration);
        };
    }

    private SubscriptionStatus parseStatus(String rawStatus) {
        try {
            return SubscriptionStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("status must be one of: " + List.of(SubscriptionStatus.values()));
        }
    }

    private SubscriptionSummaryResponse toSummary(Subscription subscription) {
        return new SubscriptionSummaryResponse(
                subscription.getId(),
                subscription.getStudentId(),
                subscription.getCourseId(),
                courseService.getCourseSummary(subscription.getCourseId()),
                subscription.getSubscriptionPlanId(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getStatus().name(),
                subscription.getPurchaseDate()
        );
    }

    private Subscription findSubscriptionOrThrow(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found."));
    }
}
