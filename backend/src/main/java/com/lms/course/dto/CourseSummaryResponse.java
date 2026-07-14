package com.lms.course.dto;

import com.lms.auth.dto.UserSummary;
import com.lms.category.dto.CategorySummaryResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Matches openapi.yaml's CourseSummary schema (Ref: SRS Chapter 5). */
public record CourseSummaryResponse(
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
        OffsetDateTime publishedAt
) {
}
