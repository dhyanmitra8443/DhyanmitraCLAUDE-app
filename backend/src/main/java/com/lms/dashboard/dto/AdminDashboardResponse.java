package com.lms.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

/** Matches openapi.yaml's AdminDashboard schema (Ref: SRS 13.4). */
public record AdminDashboardResponse(
        long totalRegisteredStudents,
        long totalActiveStudents,
        long totalInstructors,
        long totalCourses,
        long publishedCourses,
        long draftCourses,
        long archivedCourses,
        long totalCategories,
        long totalLiveClasses,
        long scheduledLiveClasses,
        long completedLiveClasses,
        long totalSubscriptionPlans,
        long activeSubscriptions,
        long expiredSubscriptions,
        long totalOrders,
        long successfulPayments,
        long failedPayments,
        BigDecimal totalRevenue,
        long certificatesIssued,
        List<RecentActivityResponse> recentActivities
) {
}
