package com.lms.dashboard.dto;

import java.util.List;

/** Matches openapi.yaml's InstructorDashboard schema (Ref: SRS 13.5). */
public record InstructorDashboardResponse(
        long totalAssignedCourses,
        long publishedCourses,
        long draftCourses,
        long totalEnrolledStudents,
        long upcomingLiveClasses,
        long completedLiveClasses,
        long certificatesIssuedForAssignedCourses,
        List<InstructorCourseSummaryResponse> courseSummaries,
        List<RecentActivityResponse> recentActivities
) {
}
