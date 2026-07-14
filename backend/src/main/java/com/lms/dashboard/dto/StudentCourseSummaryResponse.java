package com.lms.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.lms.course.dto.CourseSummaryResponse;

import java.time.LocalDate;

/**
 * Matches the myCourses entry inside openapi.yaml's StudentDashboard schema
 * (Ref: SRS 13.6) - an allOf of CourseSummary plus progressPercentage/
 * subscriptionExpiryDate. @JsonUnwrapped flattens CourseSummaryResponse's
 * fields to the top level instead of nesting them, matching the contract.
 */
public record StudentCourseSummaryResponse(
        @JsonUnwrapped CourseSummaryResponse course,
        double progressPercentage,
        LocalDate subscriptionExpiryDate
) {
}
