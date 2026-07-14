package com.lms.resource.controller;

import com.lms.config.security.UserPrincipal;
import com.lms.resource.dto.CreateLessonResourceRequest;
import com.lms.resource.dto.DownloadUrlResponse;
import com.lms.resource.dto.LessonResourceSummaryResponse;
import com.lms.resource.dto.UploadUrlRequest;
import com.lms.resource.dto.UploadUrlResponse;
import com.lms.resource.service.LessonResourceService;
import com.lms.shared.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/** Ref: SRS Chapter 8 - Lesson Resources & File Management. Matches openapi.yaml's Lesson Resources tag. */
@RestController
@RequestMapping("/api/v1")
public class LessonResourceController {

    private final LessonResourceService lessonResourceService;

    public LessonResourceController(LessonResourceService lessonResourceService) {
        this.lessonResourceService = lessonResourceService;
    }

    @GetMapping("/lessons/{lessonId}/resources")
    public ResponseEntity<ApiResponse<List<LessonResourceSummaryResponse>>> listResources(
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.of("Ordered list of resources.", lessonResourceService.listResources(lessonId, principal)));
    }

    @PostMapping("/lessons/{lessonId}/resources")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<LessonResourceSummaryResponse>> createResource(
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateLessonResourceRequest request
    ) {
        LessonResourceSummaryResponse created = lessonResourceService.createResource(lessonId, request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Resource created as ACTIVE.", created));
    }

    @PostMapping("/lesson-resources/upload-url")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<UploadUrlResponse>> issueUploadUrl(@Valid @RequestBody UploadUrlRequest request) {
        return ResponseEntity.ok(ApiResponse.of("Pre-signed upload URL issued.", lessonResourceService.issueUploadUrl(request)));
    }

    /** Not part of openapi.yaml's documented operations - the actual byte transfer the issued uploadUrl points at. */
    @PostMapping("/lesson-resources/upload/{token}")
    public ResponseEntity<ApiResponse<Void>> consumeUpload(@PathVariable String token, @RequestPart("file") MultipartFile file) {
        lessonResourceService.consumeUpload(token, file);
        return ResponseEntity.ok(ApiResponse.of("File uploaded."));
    }

    @PatchMapping("/resources/{resourceId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<LessonResourceSummaryResponse>> updateResource(
            @PathVariable UUID resourceId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateLessonResourceRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of("Resource updated.", lessonResourceService.updateResource(resourceId, request, principal)));
    }

    @PostMapping("/resources/{resourceId}/archive")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> archiveResource(
            @PathVariable UUID resourceId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        lessonResourceService.archiveResource(resourceId, principal);
        return ResponseEntity.ok(ApiResponse.of("Resource archived."));
    }

    @GetMapping("/resources/{resourceId}/download")
    public ResponseEntity<ApiResponse<DownloadUrlResponse>> issueDownloadUrl(
            @PathVariable UUID resourceId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.of("Pre-signed download URL issued.", lessonResourceService.issueDownloadUrl(resourceId, principal)));
    }

    /** Not part of openapi.yaml's documented operations - the actual byte transfer the issued downloadUrl points at. */
    @GetMapping("/lesson-resources/download/{token}")
    public ResponseEntity<Resource> consumeDownload(@PathVariable String token) {
        LessonResourceService.DownloadPayload payload = lessonResourceService.consumeDownload(token);
        MediaType contentType = payload.contentType() != null
                ? MediaType.parseMediaType(payload.contentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        String filename = payload.fileName() != null ? payload.fileName() : "download";
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(payload.resource());
    }
}
