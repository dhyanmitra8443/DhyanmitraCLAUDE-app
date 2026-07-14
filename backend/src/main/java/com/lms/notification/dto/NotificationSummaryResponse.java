package com.lms.notification.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Matches openapi.yaml's NotificationSummary schema (Ref: SRS Chapter 14). */
public record NotificationSummaryResponse(
        UUID id,
        UUID recipientUserId,
        String title,
        String message,
        String notificationType,
        String relatedModule,
        UUID relatedEntityId,
        String readStatus,
        OffsetDateTime createdAt,
        OffsetDateTime readAt
) {
}
