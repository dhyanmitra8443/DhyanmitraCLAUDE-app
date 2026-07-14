package com.lms.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ref: SRS 16.9, 17.24, 19.7 - the passphrase protecting administrator-
 * supplied credentials stored in system_settings. Externalized like every
 * other secret (JWT, Razorpay, SMTP); never hardcoded or committed.
 *
 * Rotating this value orphans anything already encrypted with the old one:
 * SecretCipher.decrypt() returns null, the affected credential reads as
 * "not configured", and the environment-variable fallback takes over until
 * an administrator re-enters it via PATCH /settings/*.
 */
@ConfigurationProperties(prefix = "app.settings")
public record SettingsProperties(
        String encryptionKey
) {
}
