package com.lms.certificate.repository;

import com.lms.certificate.entity.Certificate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

/** Ref: SRS 12.15 - optional filters for the admin certificate search endpoint. */
public final class CertificateSpecifications {

    private CertificateSpecifications() {
    }

    public static Specification<Certificate> hasCourse(UUID courseId) {
        if (courseId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("courseId"), courseId);
    }

    public static Specification<Certificate> certificateNumberContains(String certificateNumber) {
        if (certificateNumber == null || certificateNumber.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.like(root.get("certificateNumber"), "%" + certificateNumber + "%");
    }

    /** studentName is resolved to matching student IDs beforehand (Certificate has no JPA relation to User). */
    public static Specification<Certificate> studentIdIn(List<UUID> studentIds) {
        if (studentIds == null) {
            return null;
        }
        return (root, query, cb) -> root.get("studentId").in(studentIds);
    }
}
