package com.lms.lesson.dto;

import java.util.List;
import java.util.UUID;

/** Matches openapi.yaml's SectionDetail schema (Ref: SRS 7.3, 7.9, 7.10). */
public record SectionDetailResponse(
        UUID id,
        UUID courseId,
        String title,
        String shortDescription,
        Integer displayOrder,
        String status,
        List<LessonSummaryResponse> lessons
) {
}
