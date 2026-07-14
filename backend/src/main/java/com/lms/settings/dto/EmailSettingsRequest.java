package com.lms.settings.dto;

import com.lms.settings.entity.EncryptionType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Ref: SRS 16.7 - including the "Authentication Credentials" the chapter
 * requires. smtpPassword is write-only: it is stored AES-encrypted and is
 * never returned by GET /settings (EmailSettingsResponse reports only
 * whether one is configured).
 */
public record EmailSettingsRequest(
        String smtpHost,
        @Min(1) @Max(65535) Integer smtpPort,
        @Email String senderEmail,
        String senderDisplayName,
        String smtpUsername,
        String smtpPassword,
        EncryptionType encryptionType
) {
}
