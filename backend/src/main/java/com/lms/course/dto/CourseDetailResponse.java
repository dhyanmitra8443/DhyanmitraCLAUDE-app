package com.lms.course.dto;

import com.lms.auth.dto.UserSummary;
import com.lms.category.dto.CategorySummaryResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Matches openapi.yaml's CourseDetail schema (Ref: SRS 5.15).
 * subscriptionPlans is always empty until Ch.9 (Subscription Plans) exists.
 */
public record CourseDetailResponse(
        UUID id,
        String title,
        String shortDescription,
        String thumbnailUrl,
        String language,
        String difficultyLevel,
        Integer estimatedDurationMinutes,
        String status,
        List<UserSummary> instructors,
        List<CategorySummaryResponse> categories,
        int lessonCount,
        OffsetDateTime publishedAt,
        String detailedDescription,
        List<Object> subscriptionPlans,
        UUID previewLessonId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
