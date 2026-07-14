package com.lms.settings.dto;

import com.lms.settings.entity.GatewayEnvironment;

/**
 * Ref: SRS 16.9. razorpayKeySecret and razorpayWebhookSecret are write-only:
 * stored AES-encrypted and never returned by GET /settings, which reports
 * only whether each is configured.
 */
public record PaymentGatewaySettingsRequest(
        String razorpayKeyId,
        String razorpayKeySecret,
        String razorpayWebhookSecret,
        String webhookCallbackUrl,
        GatewayEnvironment environment
) {
}
