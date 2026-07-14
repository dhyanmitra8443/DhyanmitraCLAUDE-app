package com.lms.subscription.dto;

import com.lms.course.dto.CourseSummaryResponse;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Matches openapi.yaml's SubscriptionSummary schema (Ref: SRS 9.13, 9.16, 9.18). */
public record SubscriptionSummaryResponse(
        UUID id,
        UUID studentId,
        UUID courseId,
        CourseSummaryResponse course,
        UUID subscriptionPlanId,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        OffsetDateTime purchaseDate
) {
}
