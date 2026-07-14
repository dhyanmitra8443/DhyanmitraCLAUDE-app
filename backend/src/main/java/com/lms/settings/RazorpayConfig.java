package com.lms.settings;

/**
 * Ref: SRS 16.9 - the Razorpay credentials actually in force, after
 * resolving administrator-configured settings (DB, encrypted) against the
 * environment-variable fallback (RazorpayProperties). See
 * SystemSettingsService.effectiveRazorpayConfig().
 */
public record RazorpayConfig(
        String keyId,
        String keySecret,
        String webhookSecret
) {

    public boolean isConfigured() {
        return keyId != null && !keyId.isBlank() && keySecret != null && !keySecret.isBlank();
    }
}
