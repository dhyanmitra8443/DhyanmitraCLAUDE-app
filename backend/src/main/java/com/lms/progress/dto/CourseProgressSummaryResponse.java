package com.lms.progress.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Matches openapi.yaml's CourseProgressSummary schema (Ref: SRS 12.6, 12.7, 12.9). */
public record CourseProgressSummaryResponse(
        UUID studentId,
        UUID courseId,
        double progressPercentage,
        int completedLessons,
        int totalPublishedLessons,
        UUID lastCompletedLessonId,
        String completionStatus,
        OffsetDateTime courseCompletedAt
) {
}
