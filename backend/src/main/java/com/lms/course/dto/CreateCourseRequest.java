package com.lms.course.dto;

import com.lms.course.entity.DifficultyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Set;
import java.util.UUID;

/** Matches openapi.yaml's CreateCourseRequest schema (Ref: SRS 5.5, 5.11 - reused for both create and update). */
public record CreateCourseRequest(
        @NotBlank String title,
        @NotBlank String shortDescription,
        @NotBlank String detailedDescription,
        @NotBlank String thumbnailUrl,
        @NotBlank String language,
        @NotNull DifficultyLevel difficultyLevel,
        @PositiveOrZero Integer estimatedDurationMinutes,
        @NotEmpty Set<UUID> instructorIds,
        @NotEmpty Set<UUID> categoryIds
) {
}
