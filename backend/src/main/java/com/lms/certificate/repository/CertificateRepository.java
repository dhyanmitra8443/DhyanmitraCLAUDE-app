package com.lms.certificate.repository;

import com.lms.certificate.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID>, JpaSpecificationExecutor<Certificate> {

    List<Certificate> findByStudentId(UUID studentId);

    Optional<Certificate> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    Optional<Certificate> findByVerificationId(UUID verificationId);

    long countByCertificateNumberStartingWith(String prefix);

    long countByCourseIdIn(List<UUID> courseIds);

    long countByCreatedAtBetween(java.time.OffsetDateTime from, java.time.OffsetDateTime to);
}
