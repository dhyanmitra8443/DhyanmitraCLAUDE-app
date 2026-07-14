package com.lms.lesson.controller;

import com.lms.config.security.UserPrincipal;
import com.lms.lesson.dto.CreateLessonRequest;
import com.lms.lesson.dto.LessonDetailResponse;
import com.lms.lesson.dto.ReorderLessonsRequest;
import com.lms.lesson.dto.SetPreviewRequest;
import com.lms.lesson.service.LessonService;
import com.lms.shared.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Ref: SRS Chapter 7 - Section & Lesson Management (lessons). Matches openapi.yaml's Sections & Lessons tag. */
@RestController
@RequestMapping("/api/v1")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping("/sections/{sectionId}/lessons")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<LessonDetailResponse>> createLesson(
            @PathVariable UUID sectionId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateLessonRequest request
    ) {
        LessonDetailResponse created = lessonService.createLesson(sectionId, request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Lesson created as DRAFT.", created));
    }

    @PatchMapping("/sections/{sectionId}/lessons/reorder")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> reorderLessons(
            @PathVariable UUID sectionId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReorderLessonsRequest request
    ) {
        lessonService.reorderLessons(sectionId, request.lessonIdsInOrder(), principal);
        return ResponseEntity.ok(ApiResponse.of("Lessons reordered."));
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<LessonDetailResponse>> getLessonDetail(
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.of("Lesson details.", lessonService.getLessonDetail(lessonId, principal)));
    }

    @PatchMapping("/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<LessonDetailResponse>> updateLesson(
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateLessonRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of("Lesson updated.", lessonService.updateLesson(lessonId, request, principal)));
    }

    @PostMapping("/lessons/{lessonId}/publish")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> publishLesson(
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        lessonService.publishLesson(lessonId, principal);
        return ResponseEntity.ok(ApiResponse.of("Lesson published."));
    }

    @PostMapping("/lessons/{lessonId}/archive")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> archiveLesson(
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        lessonService.archiveLesson(lessonId, principal);
        return ResponseEntity.ok(ApiResponse.of("Lesson archived."));
    }

    @PatchMapping("/lessons/{lessonId}/preview")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> setPreview(
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SetPreviewRequest request
    ) {
        lessonService.setPreview(lessonId, request.isPreview(), principal);
        return ResponseEntity.ok(ApiResponse.of("Preview flag updated."));
    }
}
