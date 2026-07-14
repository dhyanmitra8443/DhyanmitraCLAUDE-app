package com.lms.settings.service;

import com.lms.settings.CertificateBranding;
import com.lms.settings.PasswordPolicy;
import com.lms.settings.RazorpayConfig;
import com.lms.settings.SecretCipher;
import com.lms.settings.SmtpConfig;
import com.lms.settings.dto.AuthenticationSettingsRequest;
import com.lms.settings.dto.BackupSettingsRequest;
import com.lms.settings.dto.CertificateSettingsRequest;
import com.lms.settings.dto.EmailSettingsRequest;
import com.lms.settings.dto.FileUploadSettingsRequest;
import com.lms.settings.dto.GeneralSettingsRequest;
import com.lms.settings.dto.LiveClassSettingsRequest;
import com.lms.settings.dto.NotificationSettingsRequest;
import com.lms.settings.dto.PaymentGatewaySettingsRequest;
import com.lms.settings.dto.SystemSettingsResponse;
import com.lms.settings.entity.SystemSettings;
import com.lms.settings.repository.SystemSettingsRepository;
import com.lms.shared.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Ref: SRS Chapter 16 - System Settings.
 *
 * This is the live control plane, not a CRUD table: the password policy
 * here is what PasswordConstraintValidator enforces, maintenanceModeEnabled
 * is what MaintenanceModeFilter checks, the notification toggles gate
 * NotificationService, and the Razorpay/SMTP credentials are what the
 * payment and email paths actually use (falling back to environment
 * configuration whenever an administrator has not set them).
 *
 * Because the read paths above run on effectively every request, the single
 * settings row is cached in memory and invalidated on write rather than
 * re-read from the database each time. That is safe for the single-container
 * monolith the SRS deploys (Ref: SRS 2.12); a multi-instance deployment
 * would need a TTL or a cache-invalidation broadcast, since one instance's
 * PATCH would not evict the others' caches.
 */
@Service
public class SystemSettingsService {

    private final SystemSettingsRepository repository;
    private final SecretCipher cipher;

    /**
     * Written only inside invalidate()/load(); volatile so a PATCH on one
     * request thread is immediately visible to every other.
     */
    private volatile SystemSettings cached;

    public SystemSettingsService(SystemSettingsRepository repository, SecretCipher cipher) {
        this.repository = repository;
        this.cipher = cipher;
    }

    // =====================================================================
    // Reads used by other modules (the "live control plane")
    // =====================================================================

    /** Ref: SRS 3.9, 16.5 - the rules PasswordConstraintValidator enforces right now. */
    @Transactional(readOnly = true)
    public PasswordPolicy passwordPolicy() {
        SystemSettings settings = cachedSettings();
        if (settings == null) {
            return PasswordPolicy.srsDefault();
        }
        return new PasswordPolicy(
                settings.getPasswordMinLength(),
                settings.isPasswordRequireUppercase(),
                settings.isPasswordRequireLowercase(),
                settings.isPasswordRequireDigit(),
                settings.isPasswordRequireSpecialChar()
        );
    }

    /** Ref: SRS 16.12. Defaults to false (system open) if settings can't be read - never lock everyone out on a read failure. */
    @Transactional(readOnly = true)
    public boolean isMaintenanceModeEnabled() {
        SystemSettings settings = cachedSettings();
        return settings != null && settings.isMaintenanceModeEnabled();
    }

    /** Ref: SRS 16.8 - channel toggles; default to enabled when unset. */
    @Transactional(readOnly = true)
    public boolean isEmailNotificationsEnabled() {
        SystemSettings settings = cachedSettings();
        return settings == null || settings.isEmailNotificationsEnabled();
    }

    @Transactional(readOnly = true)
    public boolean isInAppNotificationsEnabled() {
        SystemSettings settings = cachedSettings();
        return settings == null || settings.isInAppNotificationsEnabled();
    }

    /**
     * Ref: SRS 16.9 - administrator-configured Razorpay credentials, decrypted.
     * Fields are null when unset; the payment module merges this over its
     * environment-variable configuration (see RazorpayConfigResolver).
     */
    @Transactional(readOnly = true)
    public RazorpayConfig razorpayConfigFromSettings() {
        SystemSettings settings = cachedSettings();
        if (settings == null) {
            return new RazorpayConfig(null, null, null);
        }
        return new RazorpayConfig(
                settings.getRazorpayKeyId(),
                cipher.decrypt(settings.getRazorpayKeySecretEncrypted()),
                cipher.decrypt(settings.getRazorpayWebhookSecretEncrypted())
        );
    }

    /** Ref: SRS 16.7 - administrator-configured SMTP, decrypted. Unconfigured unless a host is set. */
    @Transactional(readOnly = true)
    public SmtpConfig smtpConfigFromSettings() {
        SystemSettings settings = cachedSettings();
        if (settings == null) {
            return new SmtpConfig(null, null, null, null, null, null, null);
        }
        return new SmtpConfig(
                settings.getSmtpHost(),
                settings.getSmtpPort(),
                settings.getSmtpUsername(),
                cipher.decrypt(settings.getSmtpPasswordEncrypted()),
                settings.getEncryptionType(),
                settings.getSenderEmail(),
                settings.getSenderDisplayName()
        );
    }

    /** Ref: SRS 16.11 - branding for certificates issued from now on. Nulls mean "use the app.certificate.* fallback". */
    @Transactional(readOnly = true)
    public CertificateBranding certificateBranding() {
        SystemSettings settings = cachedSettings();
        if (settings == null) {
            return new CertificateBranding(null, null, null, null);
        }
        return new CertificateBranding(
                settings.getCertificateOrganizationName(),
                settings.getCertificateLogoUrl(),
                settings.getCertificateSignatureUrl(),
                settings.getCertificateFooterText()
        );
    }

    // =====================================================================
    // Admin-facing read (GET /settings)
    // =====================================================================

    @Transactional(readOnly = true)
    public SystemSettingsResponse getSettings() {
        SystemSettings s = requireSettings();
        return new SystemSettingsResponse(
                new SystemSettingsResponse.General(
                        s.getOrganizationName(),
                        s.getOrganizationLogoUrl(),
                        s.getSupportEmail(),
                        s.getSupportPhone(),
                        s.getWebsiteUrl(),
                        s.getOrganizationAddress()),
                new SystemSettingsResponse.Authentication(
                        s.getSessionTimeoutMinutes(),
                        s.getMaxLoginAttempts(),
                        s.getPasswordMinLength(),
                        new SystemSettingsResponse.PasswordComplexity(
                                s.isPasswordRequireUppercase(),
                                s.isPasswordRequireLowercase(),
                                s.isPasswordRequireDigit(),
                                s.isPasswordRequireSpecialChar())),
                new SystemSettingsResponse.FileUpload(
                        s.getMaxUploadSizeMb(),
                        s.getAllowedFileTypes() == null ? null : Arrays.asList(s.getAllowedFileTypes()),
                        s.getMaxFilesPerUpload()),
                new SystemSettingsResponse.Email(
                        s.getSmtpHost(),
                        s.getSmtpPort(),
                        s.getSenderEmail(),
                        s.getSenderDisplayName(),
                        s.getSmtpUsername(),
                        isSet(s.getSmtpPasswordEncrypted()),
                        s.getEncryptionType()),
                new SystemSettingsResponse.Notifications(
                        s.isEmailNotificationsEnabled(),
                        s.isInAppNotificationsEnabled()),
                new SystemSettingsResponse.PaymentGateway(
                        s.getRazorpayKeyId(),
                        isSet(s.getRazorpayKeySecretEncrypted()),
                        isSet(s.getRazorpayWebhookSecretEncrypted()),
                        s.getWebhookCallbackUrl(),
                        s.getEnvironment()),
                new SystemSettingsResponse.LiveClass(
                        s.getDefaultTimeZone(),
                        s.getDefaultMeetingDurationMinutes(),
                        s.getDefaultReminderMinutesBefore()),
                new SystemSettingsResponse.Certificate(
                        s.getCertificateOrganizationName(),
                        s.getCertificateLogoUrl(),
                        s.getCertificateSignatureUrl(),
                        s.getCertificateFooterText()),
                new SystemSettingsResponse.Backup(
                        s.getBackupLocation(),
                        s.getBackupFrequency(),
                        s.getBackupRetentionDays()),
                s.isMaintenanceModeEnabled(),
                s.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public SystemSettingsResponse.Backup getBackupSettings() {
        SystemSettings s = requireSettings();
        return new SystemSettingsResponse.Backup(s.getBackupLocation(), s.getBackupFrequency(), s.getBackupRetentionDays());
    }

    // =====================================================================
    // Writes (PATCH /settings/*) - null field means "leave unchanged"
    // =====================================================================

    @Transactional
    public void updateGeneral(GeneralSettingsRequest request, UUID adminUserId) {
        update(adminUserId, s -> {
            if (request.organizationName() != null) s.setOrganizationName(request.organizationName());
            if (request.organizationLogoUrl() != null) s.setOrganizationLogoUrl(request.organizationLogoUrl());
            if (request.supportEmail() != null) s.setSupportEmail(request.supportEmail());
            if (request.supportPhone() != null) s.setSupportPhone(request.supportPhone());
            if (request.websiteUrl() != null) s.setWebsiteUrl(request.websiteUrl());
            if (request.organizationAddress() != null) s.setOrganizationAddress(request.organizationAddress());
        });
    }

    /** Ref: SRS 16.5 - the complexity floor is enforced against the *merged* result, not the request in isolation. */
    @Transactional
    public void updateAuthentication(AuthenticationSettingsRequest request, UUID adminUserId) {
        update(adminUserId, s -> {
            if (request.sessionTimeoutMinutes() != null) s.setSessionTimeoutMinutes(request.sessionTimeoutMinutes());
            if (request.maxLoginAttempts() != null) s.setMaxLoginAttempts(request.maxLoginAttempts());
            if (request.passwordMinLength() != null) s.setPasswordMinLength(request.passwordMinLength());

            AuthenticationSettingsRequest.PasswordComplexityRequest complexity = request.passwordComplexity();
            if (complexity != null) {
                if (complexity.requireUppercase() != null) s.setPasswordRequireUppercase(complexity.requireUppercase());
                if (complexity.requireLowercase() != null) s.setPasswordRequireLowercase(complexity.requireLowercase());
                if (complexity.requireDigit() != null) s.setPasswordRequireDigit(complexity.requireDigit());
                if (complexity.requireSpecialChar() != null) s.setPasswordRequireSpecialChar(complexity.requireSpecialChar());
            }

            assertPasswordFloor(s);
        });
    }

    @Transactional
    public void updateFileUpload(FileUploadSettingsRequest request, UUID adminUserId) {
        update(adminUserId, s -> {
            if (request.maxUploadSizeMb() != null) s.setMaxUploadSizeMb(request.maxUploadSizeMb());
            if (request.maxFilesPerUpload() != null) s.setMaxFilesPerUpload(request.maxFilesPerUpload());
            if (request.allowedFileTypes() != null) {
                s.setAllowedFileTypes(request.allowedFileTypes().toArray(new String[0]));
            }
        });
    }

    @Transactional
    public void updateEmail(EmailSettingsRequest request, UUID adminUserId) {
        update(adminUserId, s -> {
            if (request.smtpHost() != null) s.setSmtpHost(request.smtpHost());
            if (request.smtpPort() != null) s.setSmtpPort(request.smtpPort());
            if (request.senderEmail() != null) s.setSenderEmail(request.senderEmail());
            if (request.senderDisplayName() != null) s.setSenderDisplayName(request.senderDisplayName());
            if (request.smtpUsername() != null) s.setSmtpUsername(request.smtpUsername());
            if (request.encryptionType() != null) s.setEncryptionType(request.encryptionType());
            // Ref: SRS 16.7, 17.24 - stored encrypted, never in the clear.
            if (isSet(request.smtpPassword())) {
                s.setSmtpPasswordEncrypted(cipher.encrypt(request.smtpPassword()));
            }
        });
    }

    @Transactional
    public void updateNotifications(NotificationSettingsRequest request, UUID adminUserId) {
        update(adminUserId, s -> {
            if (request.emailNotificationsEnabled() != null) s.setEmailNotificationsEnabled(request.emailNotificationsEnabled());
            if (request.inAppNotificationsEnabled() != null) s.setInAppNotificationsEnabled(request.inAppNotificationsEnabled());
        });
    }

    @Transactional
    public void updatePaymentGateway(PaymentGatewaySettingsRequest request, UUID adminUserId) {
        update(adminUserId, s -> {
            if (request.razorpayKeyId() != null) s.setRazorpayKeyId(request.razorpayKeyId());
            if (request.webhookCallbackUrl() != null) s.setWebhookCallbackUrl(request.webhookCallbackUrl());
            if (request.environment() != null) s.setEnvironment(request.environment());
            // Ref: SRS 16.9 - write-only credentials, encrypted at rest. A blank
            // or absent value leaves the stored secret untouched, so an admin
            // can update the Key ID without re-typing the secret.
            if (isSet(request.razorpayKeySecret())) {
                s.setRazorpayKeySecretEncrypted(cipher.encrypt(request.razorpayKeySecret()));
            }
            if (isSet(request.razorpayWebhookSecret())) {
                s.setRazorpayWebhookSecretEncrypted(cipher.encrypt(request.razorpayWebhookSecret()));
            }
        });
    }

    @Transactional
    public void updateLiveClass(LiveClassSettingsRequest request, UUID adminUserId) {
        update(adminUserId, s -> {
            if (request.defaultTimeZone() != null) s.setDefaultTimeZone(request.defaultTimeZone());
            if (request.defaultMeetingDurationMinutes() != null) s.setDefaultMeetingDurationMinutes(request.defaultMeetingDurationMinutes());
            if (request.defaultReminderMinutesBefore() != null) s.setDefaultReminderMinutesBefore(request.defaultReminderMinutesBefore());
        });
    }

    @Transactional
    public void updateCertificate(CertificateSettingsRequest request, UUID adminUserId) {
        update(adminUserId, s -> {
            if (request.organizationName() != null) s.setCertificateOrganizationName(request.organizationName());
            if (request.organizationLogoUrl() != null) s.setCertificateLogoUrl(request.organizationLogoUrl());
            if (request.signatureImageUrl() != null) s.setCertificateSignatureUrl(request.signatureImageUrl());
            if (request.footerText() != null) s.setCertificateFooterText(request.footerText());
        });
    }

    @Transactional
    public void updateMaintenanceMode(boolean enabled, UUID adminUserId) {
        update(adminUserId, s -> s.setMaintenanceModeEnabled(enabled));
    }

    @Transactional
    public void updateBackup(BackupSettingsRequest request, UUID adminUserId) {
        update(adminUserId, s -> {
            if (request.backupLocation() != null) s.setBackupLocation(request.backupLocation());
            if (request.backupFrequency() != null) s.setBackupFrequency(request.backupFrequency());
            if (request.retentionDays() != null) s.setBackupRetentionDays(request.retentionDays());
        });
    }

    // =====================================================================
    // Internals
    // =====================================================================

    private void update(UUID adminUserId, Consumer<SystemSettings> mutation) {
        SystemSettings settings = requireSettings();
        mutation.accept(settings);
        settings.setUpdatedBy(adminUserId);
        repository.save(settings);
        cached = null; // next read reloads, picking up updated_at from the DB trigger
    }

    /**
     * Ref: SRS 16.5 - complexity can be tuned but never switched off entirely:
     * at least one letter class AND one digit-or-special requirement must
     * remain mandatory. The database enforces the same rule as a CHECK
     * constraint; this exists so an administrator gets a clear 400 rather
     * than a generic constraint-violation 409.
     */
    private void assertPasswordFloor(SystemSettings s) {
        if (!s.isPasswordRequireUppercase() && !s.isPasswordRequireLowercase()) {
            throw new BadRequestException(
                    "Password complexity cannot be fully disabled: at least one of requireUppercase or requireLowercase must stay enabled.");
        }
        if (!s.isPasswordRequireDigit() && !s.isPasswordRequireSpecialChar()) {
            throw new BadRequestException(
                    "Password complexity cannot be fully disabled: at least one of requireDigit or requireSpecialChar must stay enabled.");
        }
    }

    /** Loads (and caches) the singleton row; null only if the seed row is somehow missing. */
    private SystemSettings cachedSettings() {
        SystemSettings local = cached;
        if (local == null) {
            local = repository.findSingleton().orElse(null);
            cached = local;
        }
        return local;
    }

    private SystemSettings requireSettings() {
        return repository.findSingleton().orElseThrow(() -> new IllegalStateException(
                "system_settings row is missing - the V1 migration seeds it; the database has been tampered with."));
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }

    /** Exposed for the file-upload settings consumers; empty means "no restriction configured". */
    @Transactional(readOnly = true)
    public List<String> allowedFileTypes() {
        SystemSettings settings = cachedSettings();
        return settings == null || settings.getAllowedFileTypes() == null
                ? List.of()
                : Arrays.asList(settings.getAllowedFileTypes());
    }
}
