package com.lms.progress.controller;

import com.lms.config.security.UserPrincipal;
import com.lms.progress.dto.CourseProgressSummaryResponse;
import com.lms.progress.service.ProgressService;
import com.lms.shared.response.ApiResponse;
import com.lms.shared.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Ref: SRS Chapter 12 - Student Progress Tracking. Matches openapi.yaml's Progress & Certificates tag. */
@RestController
@RequestMapping("/api/v1")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @PostMapping("/lessons/{lessonId}/progress/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<CourseProgressSummaryResponse>> markLessonComplete(
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CourseProgressSummaryResponse progress = progressService.markLessonComplete(lessonId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.of("Lesson marked complete.", progress));
    }

    @GetMapping("/courses/{courseId}/progress/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<CourseProgressSummaryResponse>> getOwnProgress(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.of("Course progress.", progressService.getOwnProgress(courseId, principal.getUserId())));
    }

    @GetMapping("/courses/{courseId}/progress")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<PageResponse<CourseProgressSummaryResponse>>> listCourseProgress(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<CourseProgressSummaryResponse> results = progressService.listCourseProgress(courseId, principal, pageable);
        return ResponseEntity.ok(ApiResponse.of("Paginated student progress for the course.", results));
    }
}
