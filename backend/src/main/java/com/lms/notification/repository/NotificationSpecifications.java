package com.lms.notification.repository;

import com.lms.notification.entity.Notification;
import com.lms.notification.entity.NotificationType;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/** Ref: SRS 14.13 - optional filters for the admin system-wide notification log. */
public final class NotificationSpecifications {

    private NotificationSpecifications() {
    }

    public static Specification<Notification> hasRecipient(UUID userId) {
        if (userId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("recipientUserId"), userId);
    }

    public static Specification<Notification> hasDeliveryChannel(NotificationType notificationType) {
        if (notificationType == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("notificationType"), notificationType);
    }

    /**
     * Ref: SRS 14.13 - the `notificationType` query param is a free string
     * (no enum), unlike `deliveryChannel` which is exactly the
     * IN_APP/EMAIL enum already covered by hasDeliveryChannel() above; it
     * is interpreted as a contains-match against related_module (the only
     * other categorical field on a notification, e.g. "PAYMENT",
     * "CERTIFICATE") rather than a redundant restatement of deliveryChannel.
     */
    public static Specification<Notification> relatedModuleContains(String relatedModule) {
        if (relatedModule == null || relatedModule.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get("relatedModule")), "%" + relatedModule.toLowerCase() + "%");
    }
}
