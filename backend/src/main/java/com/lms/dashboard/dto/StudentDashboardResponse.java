package com.lms.dashboard.dto;

import com.lms.certificate.dto.CertificateSummaryResponse;
import com.lms.liveclass.dto.LiveClassSummaryResponse;

import java.util.List;

/** Matches openapi.yaml's StudentDashboard schema (Ref: SRS 13.6). */
public record StudentDashboardResponse(
        long activeCourses,
        long completedCourses,
        double overallLearningProgress,
        List<LiveClassSummaryResponse> upcomingLiveClasses,
        ContinueLearningResponse continueLearning,
        List<StudentCourseSummaryResponse> myCourses,
        List<CertificateSummaryResponse> certificates,
        List<RecentActivityResponse> recentActivities
) {
}
