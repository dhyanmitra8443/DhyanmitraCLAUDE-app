package com.lms.certificate.dto;

import java.time.LocalDate;

/** Ref: SRS 12.13, 12.17 - public, unauthenticated verification result. */
public record CertificateVerifyResponse(
        boolean valid,
        String studentName,
        String courseName,
        LocalDate completionDate,
        LocalDate issueDate,
        String certificateNumber
) {
}
