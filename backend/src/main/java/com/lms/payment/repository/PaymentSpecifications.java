package com.lms.payment.repository;

import com.lms.payment.entity.Payment;
import com.lms.payment.entity.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

/** Ref: SRS 10.15 - optional filters for the admin payment search endpoint. */
public final class PaymentSpecifications {

    private PaymentSpecifications() {
    }

    public static Specification<Payment> hasStatus(PaymentStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Payment> transactionReferenceContains(String transactionReference) {
        if (transactionReference == null || transactionReference.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.like(root.get("transactionReference"), "%" + transactionReference + "%");
    }
}
