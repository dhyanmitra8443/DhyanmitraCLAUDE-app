package com.lms.payment.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Ref: SRS 10.3, 10.4. */
public record CreateOrderRequest(
        @NotNull UUID courseId,
        @NotNull UUID subscriptionPlanId
) {
}
