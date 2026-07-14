package com.lms.certificate;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Ref: SRS 12.10, 12.11 - snapshotted onto every issued certificate; never re-read after issuance. */
@ConfigurationProperties(prefix = "app.certificate")
public record CertificateProperties(
        String organizationName,
        String founderName
) {
}
