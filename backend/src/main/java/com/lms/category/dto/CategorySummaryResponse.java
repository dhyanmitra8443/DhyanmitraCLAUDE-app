package com.lms.category.dto;

import com.lms.category.entity.Category;

import java.util.UUID;

/** Matches openapi.yaml's CategorySummary schema (Ref: SRS Chapter 6). */
public record CategorySummaryResponse(
        UUID id,
        String name,
        String description,
        String iconUrl,
        Integer displayOrder,
        String status,
        long courseCount
) {
    public static CategorySummaryResponse from(Category category, long courseCount) {
        return new CategorySummaryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getIconUrl(),
                category.getDisplayOrder(),
                category.getStatus().name(),
                courseCount
        );
    }
}
