package com.lms.lesson.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Matches openapi.yaml's CreateLessonRequest schema (Ref: SRS 7.7, 7.15 - reused for create and update).
 * Note: the contract has no shortDescription field here even though
 * LessonSummary exposes one (sections' equivalent request does carry it) -
 * lessons.short_description (Ref: V2 migration) is therefore never
 * settable via this endpoint today; not invented here.
 */
public record CreateLessonRequest(
        @NotBlank String title,
        String detailedDescription,
        @NotBlank String videoUrl,
        String thumbnailUrl,
        Integer displayOrder
) {
}
