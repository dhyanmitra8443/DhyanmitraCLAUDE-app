package com.lms.dashboard.service;

import com.lms.shared.exception.BadRequestException;

/** Ref: SRS 13.9 - the five analyticsType path values openapi.yaml's /analytics/{analyticsType} accepts. */
public enum AnalyticsType {
    STUDENTS,
    COURSES,
    REVENUE,
    LEARNING,
    LIVE_CLASSES;

    public static AnalyticsType fromPath(String raw) {
        try {
            return AnalyticsType.valueOf(raw.trim().toUpperCase().replace('-', '_'));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("analyticsType must be one of: students, courses, revenue, learning, live-classes");
        }
    }
}
