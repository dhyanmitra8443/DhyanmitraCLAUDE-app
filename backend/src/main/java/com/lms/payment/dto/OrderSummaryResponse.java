package com.lms.payment.dto;

import com.lms.course.dto.CourseSummaryResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Matches openapi.yaml's OrderSummary schema (Ref: SRS Chapter 10). */
public record OrderSummaryResponse(
        UUID id,
        UUID studentId,
        UUID courseId,
        CourseSummaryResponse course,
        UUID subscriptionPlanId,
        BigDecimal amount,
        String currency,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
