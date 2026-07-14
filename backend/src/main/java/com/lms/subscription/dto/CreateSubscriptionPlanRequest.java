package com.lms.subscription.dto;

import com.lms.subscription.entity.DurationUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Matches openapi.yaml's CreateSubscriptionPlanRequest schema (Ref: SRS 9.3, 9.15 - reused for create and update). */
public record CreateSubscriptionPlanRequest(
        @NotBlank String planName,
        String description,
        @NotNull @DecimalMin("0") BigDecimal price,
        @NotBlank String currency,
        @NotNull @Min(1) Integer duration,
        @NotNull DurationUnit durationUnit
) {
}
