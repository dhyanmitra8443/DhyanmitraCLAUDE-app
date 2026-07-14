package com.lms.notification.service;

import com.lms.notification.EmailService;
import com.lms.notification.dto.NotificationSummaryResponse;
import com.lms.notification.entity.Notification;
import com.lms.notification.entity.NotificationType;
import com.lms.notification.entity.ReadStatus;
import com.lms.notification.repository.NotificationRepository;
import com.lms.notification.repository.NotificationSpecifications;
import com.lms.settings.service.SystemSettingsService;
import com.lms.shared.exception.ForbiddenException;
import com.lms.shared.exception.ResourceNotFoundException;
import com.lms.shared.response.PageResponse;
import com.lms.user.entity.User;
import com.lms.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Ref: SRS Chapter 14 - Notification Management. */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SystemSettingsService settingsService;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            EmailService emailService,
            SystemSettingsService settingsService
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.settingsService = settingsService;
    }

    /**
     * Ref: SRS 14.2, 14.8 - called by other modules as a side effect of a
     * business event (payment success, certificate issuance, etc.), not
     * exposed as its own REST endpoint. Persists a row for in-product
     * visibility; when type is EMAIL, also fires the actual email via
     * EmailService (fire-and-forget, per SRS 2.11/14.15).
     *
     * Ref: SRS 16.8 - each channel is independently switchable by an
     * administrator, and each gates only its own delivery: the persisted row
     * *is* the in-app channel, so disabling in-app stops new rows, while
     * disabling email stops the send. Already-persisted notifications are
     * untouched either way ("affects future notifications only; notification
     * history shall remain available").
     */
    @Transactional
    public void create(UUID recipientUserId, String title, String message, NotificationType type, String relatedModule, UUID relatedEntityId) {
        if (settingsService.isInAppNotificationsEnabled()) {
            Notification notification = new Notification();
            notification.setRecipientUserId(recipientUserId);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setNotificationType(type);
            notification.setRelatedModule(relatedModule);
            notification.setRelatedEntityId(relatedEntityId);
            notificationRepository.save(notification);
        }

        if (type == NotificationType.EMAIL && settingsService.isEmailNotificationsEnabled()) {
            User recipient = userRepository.findById(recipientUserId).orElse(null);
            if (recipient != null) {
                emailService.send(recipient.getEmail(), title, message);
            }
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationSummaryResponse> getOwnNotifications(UUID userId, ReadStatus readStatus, Pageable pageable) {
        Page<Notification> page = readStatus == null
                ? notificationRepository.findByRecipientUserId(userId, pageable)
                : notificationRepository.findByRecipientUserIdAndReadStatus(userId, readStatus, pageable);
        return PageResponse.from(page, this::toSummary);
    }

    @Transactional
    public void markRead(UUID notificationId, UUID userId) {
        Notification notification = findOrThrow(notificationId);
        // Ref: SRS 14.5 - "Users may only mark their own notifications."
        if (!notification.getRecipientUserId().equals(userId)) {
            throw new ForbiddenException("You do not have access to this notification.");
        }
        if (notification.getReadStatus() != ReadStatus.READ) {
            notification.setReadStatus(ReadStatus.READ);
            notification.setReadAt(OffsetDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllRead(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();
        notificationRepository.findByRecipientUserIdAndReadStatus(userId, ReadStatus.UNREAD).forEach(notification -> {
            notification.setReadStatus(ReadStatus.READ);
            notification.setReadAt(now);
            notificationRepository.save(notification);
        });
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationSummaryResponse> searchNotifications(UUID userId, String relatedModule, NotificationType deliveryChannel, Pageable pageable) {
        Specification<Notification> spec = Specification.where(NotificationSpecifications.hasRecipient(userId))
                .and(NotificationSpecifications.relatedModuleContains(relatedModule))
                .and(NotificationSpecifications.hasDeliveryChannel(deliveryChannel));
        return PageResponse.from(notificationRepository.findAll(spec, pageable), this::toSummary);
    }

    private NotificationSummaryResponse toSummary(Notification notification) {
        return new NotificationSummaryResponse(
                notification.getId(),
                notification.getRecipientUserId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getNotificationType().name(),
                notification.getRelatedModule(),
                notification.getRelatedEntityId(),
                notification.getReadStatus().name(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }

    private Notification findOrThrow(UUID notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found."));
    }
}
