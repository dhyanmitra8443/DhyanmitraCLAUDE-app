package com.lms.payment.repository;

import com.lms.payment.entity.Order;
import com.lms.payment.entity.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

/** Ref: SRS 10.15 - optional filters for the admin order search endpoint. */
public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /** studentName is resolved to matching student IDs beforehand (Order has no JPA relation to User). */
    public static Specification<Order> studentIdIn(List<UUID> studentIds) {
        if (studentIds == null) {
            return null;
        }
        return (root, query, cb) -> root.get("studentId").in(studentIds);
    }
}
