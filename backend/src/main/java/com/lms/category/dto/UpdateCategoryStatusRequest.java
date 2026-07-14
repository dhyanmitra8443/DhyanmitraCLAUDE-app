package com.lms.category.dto;

import jakarta.validation.constraints.NotNull;

/** Ref: SRS 6.4, 6.8. Categories are never permanently deleted, only (de)activated. */
public record UpdateCategoryStatusRequest(
        @NotNull String status
) {
}
