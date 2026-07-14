package com.lms.certificate.controller;

import com.lms.certificate.dto.CertificateDownloadUrlResponse;
import com.lms.certificate.dto.CertificateSummaryResponse;
import com.lms.certificate.dto.CertificateVerifyResponse;
import com.lms.certificate.service.CertificateService;
import com.lms.config.security.UserPrincipal;
import com.lms.shared.response.ApiResponse;
import com.lms.shared.response.PageResponse;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Ref: SRS Chapter 12 - Certificate Management. Matches openapi.yaml's Progress & Certificates tag. */
@RestController
@RequestMapping("/api/v1")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping("/certificates/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<CertificateSummaryResponse>>> getOwnCertificates(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of("Student's earned certificates.", certificateService.getOwnCertificates(principal.getUserId())));
    }

    @GetMapping("/certificates")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<PageResponse<CertificateSummaryResponse>>> searchCertificates(
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) String certificateNumber,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<CertificateSummaryResponse> results = certificateService.searchCertificates(studentName, courseId, certificateNumber, pageable);
        return ResponseEntity.ok(ApiResponse.of("Paginated certificate list.", results));
    }

    @GetMapping("/certificates/{certificateId}")
    public ResponseEntity<ApiResponse<CertificateSummaryResponse>> getCertificateDetail(
            @PathVariable UUID certificateId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.of("Certificate details.", certificateService.getCertificateDetail(certificateId, principal)));
    }

    @GetMapping("/certificates/{certificateId}/download")
    public ResponseEntity<ApiResponse<CertificateDownloadUrlResponse>> issueDownloadUrl(
            @PathVariable UUID certificateId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CertificateDownloadUrlResponse response = certificateService.issueDownloadUrl(certificateId, principal);
        return ResponseEntity.ok(ApiResponse.of("Pre-signed, time-limited download URL for the certificate PDF.", response));
    }

    @GetMapping("/certificates/verify/{verificationId}")
    public ResponseEntity<ApiResponse<CertificateVerifyResponse>> verifyPublic(@PathVariable UUID verificationId) {
        return ResponseEntity.ok(ApiResponse.of("Certificate is valid.", certificateService.verifyPublic(verificationId)));
    }

    /** Not part of openapi.yaml's documented operations - the actual byte transfer the issued downloadUrl points at (Ref: Ch.8's identical pattern). */
    @GetMapping("/certificate-files/download/{token}")
    public ResponseEntity<Resource> consumeDownload(@PathVariable String token) {
        Resource resource = certificateService.consumeDownload(token);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("certificate.pdf").build().toString())
                .body(resource);
    }
}
