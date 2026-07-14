package com.lms.subscription.repository;

import com.lms.subscription.entity.Subscription;
import com.lms.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

/** Ref: SRS 9.16 - optional filters for the admin subscription search endpoint. */
public final class SubscriptionSpecifications {

    private SubscriptionSpecifications() {
    }

    public static Specification<Subscription> hasCourse(UUID courseId) {
        if (courseId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("courseId"), courseId);
    }

    public static Specification<Subscription> hasStatus(SubscriptionStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /** studentName is resolved to matching student IDs beforehand (Subscription has no JPA relation to User). */
    public static Specification<Subscription> studentIdIn(List<UUID> studentIds) {
        if (studentIds == null) {
            return null;
        }
        return (root, query, cb) -> root.get("studentId").in(studentIds);
    }
}
