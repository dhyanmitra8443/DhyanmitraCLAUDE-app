package com.lms.subscription.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Matches openapi.yaml's SubscriptionPlanSummary schema (Ref: SRS Chapter 9). */
public record SubscriptionPlanSummaryResponse(
        UUID id,
        UUID courseId,
        String planName,
        String description,
        BigDecimal price,
        String currency,
        Integer duration,
        String durationUnit,
        String status
) {
}
