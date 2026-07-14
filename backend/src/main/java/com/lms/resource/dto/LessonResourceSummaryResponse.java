package com.lms.resource.dto;

import java.util.UUID;

/** Matches openapi.yaml's LessonResourceSummary schema (Ref: SRS Chapter 8). */
public record LessonResourceSummaryResponse(
        UUID id,
        UUID lessonId,
        String resourceType,
        String displayName,
        String description,
        String externalUrl,
        String fileReference,
        Long fileSizeBytes,
        Integer displayOrder,
        String status
) {
}
