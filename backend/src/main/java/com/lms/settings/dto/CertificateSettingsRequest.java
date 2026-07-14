package com.lms.settings.dto;

import jakarta.validation.constraints.Size;

/**
 * Ref: SRS 16.11 - applies only to newly generated certificates; already
 * issued certificates snapshot their branding at issuance and never change.
 */
public record CertificateSettingsRequest(
        @Size(max = 200) String organizationName,
        String organizationLogoUrl,
        String signatureImageUrl,
        String footerText
) {
}
