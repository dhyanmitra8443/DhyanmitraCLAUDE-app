package com.lms.settings;

import com.lms.settings.entity.EncryptionType;

/**
 * Ref: SRS 16.7 - the SMTP settings actually in force. Only meaningful when
 * an administrator has configured a host via PATCH /settings/email; when
 * they haven't, EmailService keeps using the JavaMailSender built from
 * environment configuration at startup.
 */
public record SmtpConfig(
        String host,
        Integer port,
        String username,
        String password,
        EncryptionType encryptionType,
        String senderEmail,
        String senderDisplayName
) {

    public boolean isConfigured() {
        return host != null && !host.isBlank();
    }
}
