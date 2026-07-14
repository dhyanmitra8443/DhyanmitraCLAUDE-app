package com.lms.liveclass.controller;

import com.lms.config.security.UserPrincipal;
import com.lms.liveclass.dto.AddRecordingUrlRequest;
import com.lms.liveclass.dto.CreateLiveClassRequest;
import com.lms.liveclass.dto.JoinLiveClassResponse;
import com.lms.liveclass.dto.LiveClassSummaryResponse;
import com.lms.liveclass.entity.LiveClassStatus;
import com.lms.liveclass.service.LiveClassService;
import com.lms.shared.response.ApiResponse;
import com.lms.shared.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/** Ref: SRS Chapter 11 - Live Classes. Matches openapi.yaml's Live Classes tag. */
@RestController
@RequestMapping("/api/v1")
public class LiveClassController {

    private final LiveClassService liveClassService;

    public LiveClassController(LiveClassService liveClassService) {
        this.liveClassService = liveClassService;
    }

    @GetMapping("/courses/{courseId}/live-classes")
    public ResponseEntity<ApiResponse<PageResponse<LiveClassSummaryResponse>>> listByCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.of("Live classes for this course.", liveClassService.listByCourse(courseId, principal, pageable)));
    }

    @PostMapping("/courses/{courseId}/live-classes")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<LiveClassSummaryResponse>> createLiveClass(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateLiveClassRequest request
    ) {
        LiveClassSummaryResponse created = liveClassService.createLiveClass(courseId, request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Live class scheduled.", created));
    }

    @GetMapping("/live-classes")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<PageResponse<LiveClassSummaryResponse>>> searchLiveClasses(
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) LiveClassStatus status,
            @RequestParam(required = false) LocalDate date,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<LiveClassSummaryResponse> results = liveClassService.searchLiveClasses(courseId, status, date, pageable);
        return ResponseEntity.ok(ApiResponse.of("Paginated live class list.", results));
    }

    @GetMapping("/live-classes/{liveClassId}")
    public ResponseEntity<ApiResponse<LiveClassSummaryResponse>> getLiveClassDetail(
            @PathVariable UUID liveClassId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.of("Live class details.", liveClassService.getLiveClassDetail(liveClassId, principal)));
    }

    @PatchMapping("/live-classes/{liveClassId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> updateLiveClass(
            @PathVariable UUID liveClassId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateLiveClassRequest request
    ) {
        liveClassService.updateLiveClass(liveClassId, request, principal);
        return ResponseEntity.ok(ApiResponse.of("Live class updated."));
    }

    @PostMapping("/live-classes/{liveClassId}/cancel")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> cancelLiveClass(
            @PathVariable UUID liveClassId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        liveClassService.cancelLiveClass(liveClassId, principal);
        return ResponseEntity.ok(ApiResponse.of("Live class cancelled."));
    }

    @PostMapping("/live-classes/{liveClassId}/join")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<JoinLiveClassResponse>> joinLiveClass(
            @PathVariable UUID liveClassId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        JoinLiveClassResponse response = liveClassService.joinLiveClass(liveClassId, principal);
        return ResponseEntity.ok(ApiResponse.of("Attendance recorded; meeting URL returned.", response));
    }

    @PostMapping("/live-classes/{liveClassId}/recording")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> addRecordingUrl(
            @PathVariable UUID liveClassId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddRecordingUrlRequest request
    ) {
        liveClassService.addRecordingUrl(liveClassId, request.recordingUrl(), principal);
        return ResponseEntity.ok(ApiResponse.of("Recording URL added."));
    }
}
