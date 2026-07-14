package com.lms.category.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** Matches openapi.yaml's CreateCategoryRequest schema (Ref: SRS 6.5, 6.7 - reused for create and update). */
public record CreateCategoryRequest(
        @NotBlank String name,
        String description,
        String iconUrl,
        @Min(1) Integer displayOrder
) {
}
