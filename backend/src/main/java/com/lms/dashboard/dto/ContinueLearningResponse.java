package com.lms.dashboard.dto;

import java.util.UUID;

/** Matches the continueLearning object inside openapi.yaml's StudentDashboard schema (Ref: SRS 13.6). */
public record ContinueLearningResponse(
        UUID courseId,
        UUID lastCompletedLessonId,
        UUID nextLessonId
) {
}
