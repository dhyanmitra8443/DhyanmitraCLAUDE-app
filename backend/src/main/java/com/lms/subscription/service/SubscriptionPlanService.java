package com.lms.subscription.service;

import com.lms.course.repository.CourseRepository;
import com.lms.shared.exception.BadRequestException;
import com.lms.shared.exception.ResourceNotFoundException;
import com.lms.subscription.dto.CreateSubscriptionPlanRequest;
import com.lms.subscription.dto.SubscriptionPlanSummaryResponse;
import com.lms.subscription.entity.PlanStatus;
import com.lms.subscription.entity.SubscriptionPlan;
import com.lms.subscription.repository.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Ref: SRS Chapter 9 - Subscription Plans & Student Enrollments (plans). */
@Service
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final CourseRepository courseRepository;

    public SubscriptionPlanService(SubscriptionPlanRepository subscriptionPlanRepository, CourseRepository courseRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlanSummaryResponse> listPlans(UUID courseId, boolean isAdmin) {
        // Ref: SRS 9.6 - public callers see only ACTIVE plans; admins see all.
        List<SubscriptionPlan> plans = isAdmin
                ? subscriptionPlanRepository.findByCourseId(courseId)
                : subscriptionPlanRepository.findByCourseIdAndStatus(courseId, PlanStatus.ACTIVE);
        return plans.stream().map(this::toSummary).toList();
    }

    @Transactional
    public SubscriptionPlanSummaryResponse createPlan(UUID courseId, CreateSubscriptionPlanRequest request) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found.");
        }
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setCourseId(courseId);
        applyRequest(plan, request);
        return toSummary(subscriptionPlanRepository.save(plan));
    }

    @Transactional
    public void updatePlan(UUID planId, CreateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = findPlanOrThrow(planId);
        applyRequest(plan, request);
        subscriptionPlanRepository.save(plan);
    }

    @Transactional
    public void updateStatus(UUID planId, String rawStatus) {
        SubscriptionPlan plan = findPlanOrThrow(planId);
        plan.setStatus(parseStatus(rawStatus));
        subscriptionPlanRepository.save(plan);
    }

    private PlanStatus parseStatus(String rawStatus) {
        try {
            return PlanStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("status must be one of: " + List.of(PlanStatus.values()));
        }
    }

    private void applyRequest(SubscriptionPlan plan, CreateSubscriptionPlanRequest request) {
        plan.setPlanName(request.planName());
        plan.setDescription(request.description());
        plan.setPrice(request.price());
        plan.setCurrency(request.currency());
        plan.setDuration(request.duration());
        plan.setDurationUnit(request.durationUnit());
    }

    private SubscriptionPlanSummaryResponse toSummary(SubscriptionPlan plan) {
        return new SubscriptionPlanSummaryResponse(
                plan.getId(),
                plan.getCourseId(),
                plan.getPlanName(),
                plan.getDescription(),
                plan.getPrice(),
                plan.getCurrency(),
                plan.getDuration(),
                plan.getDurationUnit().name(),
                plan.getStatus().name()
        );
    }

    private SubscriptionPlan findPlanOrThrow(UUID planId) {
        return subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found."));
    }
}
