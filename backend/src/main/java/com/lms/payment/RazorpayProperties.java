package com.lms.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ref: SRS 10.12, 18.24 - Razorpay credentials, externalized and never
 * hardcoded (bound from `app.razorpay.*`, sourced from environment
 * variables / a git-ignored .env in non-local environments - see
 * application.yml and the JWT/SMTP secrets for the established pattern).
 */
@ConfigurationProperties(prefix = "app.razorpay")
public record RazorpayProperties(
        String keyId,
        String keySecret,
        String webhookSecret
) {
    public boolean isConfigured() {
        return keyId != null && !keyId.isBlank() && keySecret != null && !keySecret.isBlank();
    }
}
