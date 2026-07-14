package com.lms.subscription.repository;

import com.lms.subscription.entity.PlanStatus;
import com.lms.subscription.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    List<SubscriptionPlan> findByCourseId(UUID courseId);

    List<SubscriptionPlan> findByCourseIdAndStatus(UUID courseId, PlanStatus status);

    boolean existsByCourseIdAndStatus(UUID courseId, PlanStatus status);
}
