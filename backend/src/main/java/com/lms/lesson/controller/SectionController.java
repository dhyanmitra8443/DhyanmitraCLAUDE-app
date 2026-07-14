package com.lms.lesson.controller;

import com.lms.config.security.UserPrincipal;
import com.lms.lesson.dto.CreateSectionRequest;
import com.lms.lesson.dto.ReorderSectionsRequest;
import com.lms.lesson.dto.SectionDetailResponse;
import com.lms.lesson.service.SectionService;
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

import java.util.List;
import java.util.UUID;

/** Ref: SRS Chapter 7 - Section & Lesson Management (sections). Matches openapi.yaml's Sections & Lessons tag. */
@RestController
@RequestMapping("/api/v1")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping("/courses/{courseId}/sections")
    public ResponseEntity<ApiResponse<List<SectionDetailResponse>>> getCourseOutline(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.of("Ordered list of sections.", sectionService.getCourseOutline(courseId, principal)));
    }

    @PostMapping("/courses/{courseId}/sections")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<SectionDetailResponse>> createSection(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateSectionRequest request
    ) {
        SectionDetailResponse created = sectionService.createSection(courseId, request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Section created as DRAFT.", created));
    }

    @PatchMapping("/courses/{courseId}/sections/reorder")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> reorderSections(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReorderSectionsRequest request
    ) {
        sectionService.reorderSections(courseId, request.sectionIdsInOrder(), principal);
        return ResponseEntity.ok(ApiResponse.of("Sections reordered."));
    }

    @PatchMapping("/sections/{sectionId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<SectionDetailResponse>> updateSection(
            @PathVariable UUID sectionId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateSectionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of("Section updated.", sectionService.updateSection(sectionId, request, principal)));
    }

    @PostMapping("/sections/{sectionId}/publish")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> publishSection(
            @PathVariable UUID sectionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        sectionService.publishSection(sectionId, principal);
        return ResponseEntity.ok(ApiResponse.of("Section published."));
    }

    @PostMapping("/sections/{sectionId}/archive")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> archiveSection(
            @PathVariable UUID sectionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        sectionService.archiveSection(sectionId, principal);
        return ResponseEntity.ok(ApiResponse.of("Section archived."));
    }
}
