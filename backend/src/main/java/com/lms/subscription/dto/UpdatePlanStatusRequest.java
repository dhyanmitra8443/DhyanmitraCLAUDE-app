package com.lms.subscription.dto;

import jakarta.validation.constraints.NotNull;

/** Ref: SRS 9.5, 9.15. */
public record UpdatePlanStatusRequest(
        @NotNull String status
) {
}
