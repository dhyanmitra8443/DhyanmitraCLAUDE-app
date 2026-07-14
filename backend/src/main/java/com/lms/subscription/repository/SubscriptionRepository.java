package com.lms.subscription.repository;

import com.lms.subscription.entity.Subscription;
import com.lms.subscription.entity.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID>, JpaSpecificationExecutor<Subscription> {

    List<Subscription> findByStudentId(UUID studentId);

    Page<Subscription> findByCourseId(UUID courseId, Pageable pageable);

    Optional<Subscription> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    boolean existsByStudentIdAndCourseIdAndStatusAndEndDateGreaterThanEqual(
            UUID studentId, UUID courseId, SubscriptionStatus status, LocalDate onOrAfter);

    long countByStatus(SubscriptionStatus status);

    long countByStudentIdAndStatus(UUID studentId, SubscriptionStatus status);

    long countByCourseId(UUID courseId);

    long countByCourseIdAndStatus(UUID courseId, SubscriptionStatus status);

    long countByCourseIdIn(List<UUID> courseIds);

    long countByCourseIdInAndStatus(List<UUID> courseIds, SubscriptionStatus status);
}
