package com.lms.payment.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Matches openapi.yaml's PaymentSummary schema (Ref: SRS 10.6, 10.15). */
public record PaymentSummaryResponse(
        UUID id,
        UUID orderId,
        UUID studentId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String transactionReference,
        String status,
        OffsetDateTime paymentDate
) {
}
