package com.lms.payment;

import com.lms.settings.RazorpayConfig;
import com.lms.settings.service.SystemSettingsService;
import org.springframework.stereotype.Service;

/**
 * Ref: SRS 16.9 - Razorpay credentials are administrator-configurable, but
 * they also have to exist before an administrator has ever opened the
 * settings screen (and in CI, where there is no admin at all).
 *
 * Resolution is per-field, not all-or-nothing: whatever the administrator
 * has stored in system_settings wins, and anything they have left unset
 * falls back to the environment configuration in RazorpayProperties. That
 * means setting only a Key ID in the admin UI cannot silently blank out a
 * webhook secret supplied by the environment.
 */
@Service
public class RazorpayConfigResolver {

    private final SystemSettingsService settingsService;
    private final RazorpayProperties environmentProperties;

    public RazorpayConfigResolver(SystemSettingsService settingsService, RazorpayProperties environmentProperties) {
        this.settingsService = settingsService;
        this.environmentProperties = environmentProperties;
    }

    public RazorpayConfig resolve() {
        RazorpayConfig configured = settingsService.razorpayConfigFromSettings();
        return new RazorpayConfig(
                firstSet(configured.keyId(), environmentProperties.keyId()),
                firstSet(configured.keySecret(), environmentProperties.keySecret()),
                firstSet(configured.webhookSecret(), environmentProperties.webhookSecret())
        );
    }

    private static String firstSet(String preferred, String fallback) {
        return preferred != null && !preferred.isBlank() ? preferred : fallback;
    }
}
