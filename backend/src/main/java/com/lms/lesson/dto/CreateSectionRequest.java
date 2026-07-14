package com.lms.lesson.dto;

import jakarta.validation.constraints.NotBlank;

/** Matches openapi.yaml's CreateSectionRequest schema (Ref: SRS 7.4, 7.16 - reused for create and update). */
public record CreateSectionRequest(
        @NotBlank String title,
        String shortDescription,
        Integer displayOrder
) {
}
