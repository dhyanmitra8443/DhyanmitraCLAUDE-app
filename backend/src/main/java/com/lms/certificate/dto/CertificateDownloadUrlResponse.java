package com.lms.certificate.dto;

/** Ref: SRS 12.12, 17.24. */
public record CertificateDownloadUrlResponse(
        String downloadUrl,
        long expiresInSeconds
) {
}
