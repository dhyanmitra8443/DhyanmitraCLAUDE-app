package com.lms.settings.dto;

import com.lms.settings.entity.BackupFrequency;
import com.lms.settings.entity.EncryptionType;
import com.lms.settings.entity.GatewayEnvironment;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Ref: SRS 16.1-16.13 - the whole configuration, grouped by category, for
 * the admin settings screen.
 *
 * No credential is ever echoed back (SRS 16.9: Key Secret and Webhook
 * Secret are write-only; the same rule is applied to the SMTP password).
 * Instead each secret is reported as a *Configured boolean, which is what
 * an admin UI actually needs in order to render "Configured / Not set"
 * without ever handling the value.
 */
public record SystemSettingsResponse(
        General general,
        Authentication authentication,
        FileUpload fileUpload,
        Email email,
        Notifications notifications,
        PaymentGateway paymentGateway,
        LiveClass liveClass,
        Certificate certificate,
        Backup backup,
        boolean maintenanceModeEnabled,
        OffsetDateTime updatedAt
) {

    public record General(
            String organizationName,
            String organizationLogoUrl,
            String supportEmail,
            String supportPhone,
            String websiteUrl,
            String organizationAddress
    ) {
    }

    public record Authentication(
            Integer sessionTimeoutMinutes,
            Integer maxLoginAttempts,
            int passwordMinLength,
            PasswordComplexity passwordComplexity
    ) {
    }

    public record PasswordComplexity(
            boolean requireUppercase,
            boolean requireLowercase,
            boolean requireDigit,
            boolean requireSpecialChar
    ) {
    }

    public record FileUpload(
            Integer maxUploadSizeMb,
            List<String> allowedFileTypes,
            Integer maxFilesPerUpload
    ) {
    }

    public record Email(
            String smtpHost,
            Integer smtpPort,
            String senderEmail,
            String senderDisplayName,
            String smtpUsername,
            boolean smtpPasswordConfigured,
            EncryptionType encryptionType
    ) {
    }

    public record Notifications(
            boolean emailNotificationsEnabled,
            boolean inAppNotificationsEnabled
    ) {
    }

    public record PaymentGateway(
            String razorpayKeyId,
            boolean razorpayKeySecretConfigured,
            boolean razorpayWebhookSecretConfigured,
            String webhookCallbackUrl,
            GatewayEnvironment environment
    ) {
    }

    public record LiveClass(
            String defaultTimeZone,
            Integer defaultMeetingDurationMinutes,
            Integer defaultReminderMinutesBefore
    ) {
    }

    public record Certificate(
            String organizationName,
            String organizationLogoUrl,
            String signatureImageUrl,
            String footerText
    ) {
    }

    public record Backup(
            String backupLocation,
            BackupFrequency backupFrequency,
            Integer retentionDays
    ) {
    }
}
