package com.lms.notification;

import com.lms.settings.SmtpConfig;
import com.lms.settings.entity.EncryptionType;
import com.lms.settings.service.SystemSettingsService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Ref: SRS 2.11, 14.8, 14.15 - "Email delivery shall occur asynchronously"
 * and "Business operations shall never depend on successful email
 * delivery." Every send is fire-and-forget from the caller's perspective:
 * failures are logged here and never propagate back to the triggering
 * request (registration, invitation, password reset, etc.).
 *
 * Ref: SRS 16.7 - the SMTP server is administrator-configurable. When one
 * has been configured via PATCH /settings/email, a JavaMailSender is built
 * from those settings and used instead of the environment-configured bean;
 * otherwise the environment bean (Mailpit in dev, a real relay in prod) is
 * used unchanged. The derived sender is cached and rebuilt only when the
 * settings actually change, so a config change takes effect on the next
 * email without reconstructing a mail session per message.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender environmentMailSender;
    private final SystemSettingsService settingsService;
    private final String environmentFromAddress;

    /** The settings the cached sender was built from, paired with that sender. */
    private final AtomicReference<CachedSender> cachedSender = new AtomicReference<>();

    public EmailService(
            JavaMailSender environmentMailSender,
            SystemSettingsService settingsService,
            @Value("${app.mail.from}") String environmentFromAddress
    ) {
        this.environmentMailSender = environmentMailSender;
        this.settingsService = settingsService;
        this.environmentFromAddress = environmentFromAddress;
    }

    @Async("notificationExecutor")
    public void send(String to, String subject, String body) {
        SmtpConfig config = settingsService.smtpConfigFromSettings();
        JavaMailSender sender = config.isConfigured() ? senderFor(config) : environmentMailSender;

        String fromAddress = config.senderEmail() != null && !config.senderEmail().isBlank()
                ? config.senderEmail()
                : environmentFromAddress;

        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            // A display name is optional (SRS 16.7); without one the raw address is used.
            if (config.senderDisplayName() != null && !config.senderDisplayName().isBlank()) {
                helper.setFrom(new InternetAddress(fromAddress, config.senderDisplayName()));
            } else {
                helper.setFrom(fromAddress);
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            sender.send(message);
            log.info("Email sent to {} (subject: {})", to, subject);
        } catch (MailException | jakarta.mail.MessagingException | UnsupportedEncodingException ex) {
            // Ref: SRS 14.15 - never let a mail failure surface to the caller.
            log.warn("Failed to send email to {} (subject: {}): {}", to, subject, ex.getMessage());
        }
    }

    /** Builds (and caches) a sender for the administrator-configured SMTP server. */
    private JavaMailSender senderFor(SmtpConfig config) {
        CachedSender current = cachedSender.get();
        if (current != null && current.config().equals(config)) {
            return current.sender();
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.host());
        if (config.port() != null) {
            sender.setPort(config.port());
        }
        sender.setUsername(config.username());
        sender.setPassword(config.password());

        Properties properties = sender.getJavaMailProperties();
        // Only authenticate when the administrator actually supplied credentials -
        // forcing auth=true against an open relay (e.g. a local catcher) fails.
        boolean authenticate = config.username() != null && !config.username().isBlank();
        properties.put("mail.smtp.auth", String.valueOf(authenticate));
        properties.put("mail.smtp.starttls.enable", String.valueOf(config.encryptionType() == EncryptionType.TLS));
        if (config.encryptionType() == EncryptionType.SSL) {
            properties.put("mail.smtp.ssl.enable", "true");
        }

        cachedSender.set(new CachedSender(config, sender));
        log.info("SMTP sender rebuilt from system settings (host: {})", config.host());
        return sender;
    }

    private record CachedSender(SmtpConfig config, JavaMailSender sender) {
    }
}
