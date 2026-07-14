package com.lms.dashboard.dto;

import java.time.OffsetDateTime;

/** Matches openapi.yaml's RecentActivity schema (Ref: SRS Chapter 13). */
public record RecentActivityResponse(
        String type,
        String message,
        OffsetDateTime occurredAt
) {
}
