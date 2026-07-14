package com.lms.settings;

/**
 * Ref: SRS 16.11 - branding applied to newly generated certificates.
 * Resolved by SystemSettingsService against the app.certificate.*
 * environment fallback, so a certificate always has an organization name
 * even before an administrator visits the settings screen.
 */
public record CertificateBranding(
        String organizationName,
        String logoUrl,
        String signatureUrl,
        String footerText
) {
}
