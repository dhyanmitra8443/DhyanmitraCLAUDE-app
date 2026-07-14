package com.lms.dashboard.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Matches the courseSummaries entry inside openapi.yaml's InstructorDashboard schema (Ref: SRS 13.5). */
public record InstructorCourseSummaryResponse(
        UUID courseId,
        String courseName,
        long totalStudents,
        long activeStudents,
        OffsetDateTime nextLiveClassAt,
        String courseStatus
) {
}
