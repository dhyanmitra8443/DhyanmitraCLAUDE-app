package com.lms.lesson.dto;

import java.util.List;
import java.util.UUID;

/**
 * Matches openapi.yaml's LessonDetail schema (Ref: SRS 7.14).
 * resources is always empty until Ch.8 (Lesson Resources) exists.
 */
public record LessonDetailResponse(
        UUID id,
        UUID sectionId,
        String title,
        String shortDescription,
        Integer videoDurationSeconds,
        String thumbnailUrl,
        Integer displayOrder,
        String status,
        boolean isPreview,
        String completionStatus,
        String detailedDescription,
        String videoUrl,
        List<Object> resources
) {
}
