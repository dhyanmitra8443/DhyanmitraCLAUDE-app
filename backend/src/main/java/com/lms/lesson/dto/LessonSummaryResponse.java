package com.lms.lesson.dto;

import java.util.UUID;

/**
 * Matches openapi.yaml's LessonSummary schema (Ref: SRS Chapter 7).
 * completionStatus is always null until Ch.12 (Progress Tracking) exists.
 */
public record LessonSummaryResponse(
        UUID id,
        UUID sectionId,
        String title,
        String shortDescription,
        Integer videoDurationSeconds,
        String thumbnailUrl,
        Integer displayOrder,
        String status,
        boolean isPreview,
        String completionStatus
) {
}
