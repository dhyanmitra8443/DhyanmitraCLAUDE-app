package com.lms.certificate.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Matches openapi.yaml's CertificateSummary schema (Ref: SRS Chapter 12). */
public record CertificateSummaryResponse(
        UUID id,
        String certificateNumber,
        UUID verificationId,
        UUID studentId,
        String studentName,
        UUID courseId,
        String courseName,
        List<String> instructorNames,
        LocalDate completionDate,
        LocalDate issueDate
) {
}
