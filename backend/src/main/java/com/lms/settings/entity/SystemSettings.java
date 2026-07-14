package com.lms.settings.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Ref: SRS Chapter 16 - System Settings. Exactly one row ever exists: the
 * migration seeds it, and the DB's UNIQUE + CHECK on the always-true
 * `singleton` column makes a second INSERT impossible. Callers therefore
 * never create this entity - they load the one row and mutate it.
 *
 * The three *_encrypted columns hold AES-GCM ciphertext produced by
 * SecretCipher, never plaintext (Ref: SRS 16.9, 17.24 - credentials at
 * rest). No created_at column exists, so this does not extend BaseEntity.
 */
@Getter
@Setter
@Entity
@Table(name = "system_settings")
public class SystemSettings {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    // --- 16.4 General ------------------------------------------------------
    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "organization_logo_url")
    private String organizationLogoUrl;

    @Column(name = "support_email", columnDefinition = "citext")
    private String supportEmail;

    @Column(name = "support_phone")
    private String supportPhone;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "organization_address")
    private String organizationAddress;

    // --- 16.5 Authentication ----------------------------------------------
    @Column(name = "session_timeout_minutes")
    private Integer sessionTimeoutMinutes;

    @Column(name = "max_login_attempts")
    private Integer maxLoginAttempts;

    @Column(name = "password_min_length", nullable = false)
    private int passwordMinLength = 8;

    @Column(name = "password_require_uppercase", nullable = false)
    private boolean passwordRequireUppercase = true;

    @Column(name = "password_require_lowercase", nullable = false)
    private boolean passwordRequireLowercase = true;

    @Column(name = "password_require_digit", nullable = false)
    private boolean passwordRequireDigit = true;

    @Column(name = "password_require_special_char", nullable = false)
    private boolean passwordRequireSpecialChar = true;

    // --- 16.6 File upload --------------------------------------------------
    @Column(name = "max_upload_size_mb")
    private Integer maxUploadSizeMb;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_file_types")
    private String[] allowedFileTypes;

    @Column(name = "max_files_per_upload")
    private Integer maxFilesPerUpload;

    // --- 16.7 Email --------------------------------------------------------
    @Column(name = "smtp_host")
    private String smtpHost;

    @Column(name = "smtp_port")
    private Integer smtpPort;

    @Column(name = "sender_email", columnDefinition = "citext")
    private String senderEmail;

    @Column(name = "sender_display_name")
    private String senderDisplayName;

    @Column(name = "smtp_username")
    private String smtpUsername;

    @Column(name = "smtp_password_encrypted")
    private String smtpPasswordEncrypted;

    @Enumerated(EnumType.STRING)
    @Column(name = "encryption_type")
    private EncryptionType encryptionType;

    // --- 16.8 Notifications ------------------------------------------------
    @Column(name = "email_notifications_enabled", nullable = false)
    private boolean emailNotificationsEnabled = true;

    @Column(name = "in_app_notifications_enabled", nullable = false)
    private boolean inAppNotificationsEnabled = true;

    // --- 16.9 Payment gateway (Razorpay) -----------------------------------
    @Column(name = "razorpay_key_id")
    private String razorpayKeyId;

    @Column(name = "razorpay_key_secret_encrypted")
    private String razorpayKeySecretEncrypted;

    @Column(name = "razorpay_webhook_secret_encrypted")
    private String razorpayWebhookSecretEncrypted;

    @Column(name = "webhook_callback_url")
    private String webhookCallbackUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false)
    private GatewayEnvironment environment = GatewayEnvironment.SANDBOX;

    // --- 16.10 Live class defaults -----------------------------------------
    @Column(name = "default_time_zone")
    private String defaultTimeZone;

    @Column(name = "default_meeting_duration_minutes")
    private Integer defaultMeetingDurationMinutes;

    @Column(name = "default_reminder_minutes_before")
    private Integer defaultReminderMinutesBefore;

    // --- 16.11 Certificate branding ----------------------------------------
    @Column(name = "certificate_organization_name")
    private String certificateOrganizationName;

    @Column(name = "certificate_logo_url")
    private String certificateLogoUrl;

    @Column(name = "certificate_signature_url")
    private String certificateSignatureUrl;

    @Column(name = "certificate_footer_text")
    private String certificateFooterText;

    // --- 16.12 Maintenance mode --------------------------------------------
    @Column(name = "maintenance_mode_enabled", nullable = false)
    private boolean maintenanceModeEnabled = false;

    // --- 16.13 Backup ------------------------------------------------------
    @Column(name = "backup_location")
    private String backupLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "backup_frequency")
    private BackupFrequency backupFrequency;

    @Column(name = "backup_retention_days")
    private Integer backupRetentionDays;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}
