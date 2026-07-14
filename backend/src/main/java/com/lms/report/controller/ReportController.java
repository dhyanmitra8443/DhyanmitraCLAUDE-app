package com.lms.report.controller;

import com.lms.config.security.UserPrincipal;
import com.lms.report.ReportCriteria;
import com.lms.report.ReportKey;
import com.lms.report.dto.ExportJobResponse;
import com.lms.report.dto.ExportStatusResponse;
import com.lms.report.dto.ReportDataResponse;
import com.lms.report.dto.ReportDefinitionResponse;
import com.lms.report.entity.ReportFormat;
import com.lms.report.service.ReportExportService;
import com.lms.report.service.ReportService;
import com.lms.shared.response.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Ref: SRS Chapter 15 - Reports Management. Matches openapi.yaml's Reports tag.
 *
 * There is no @PreAuthorize here because the authorization rule is not
 * "which role may call this endpoint" but "which reports may this role run" -
 * every role can call all four endpoints, and ReportService rejects any
 * reportKey the caller's role does not own with a 403 (SRS 15.3).
 */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService exportService;

    public ReportController(ReportService reportService, ReportExportService exportService) {
        this.reportService = reportService;
        this.exportService = exportService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportDefinitionResponse>>> listReports(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                "Reports available to your role.",
                reportService.listAvailableReports(principal)));
    }

    @GetMapping("/{reportKey}")
    public ResponseEntity<ApiResponse<ReportDataResponse>> getReportData(
            @PathVariable ReportKey reportKey,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ReportDataResponse data = reportService.getReportData(
                reportKey, principal, dateFrom, dateTo, courseId, search, page, size);
        return ResponseEntity.ok(ApiResponse.of(reportKey.title() + " report.", data));
    }

    /**
     * Ref: SRS 15.9, 15.15 - returns the file directly when it is small
     * enough to render inline, or 202 with a pollable job id when it is not.
     */
    @GetMapping("/{reportKey}/export")
    public ResponseEntity<?> exportReport(
            @PathVariable ReportKey reportKey,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam ReportFormat format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) String search
    ) {
        ReportCriteria criteria = reportService.criteriaFor(reportKey, principal, dateFrom, dateTo, courseId, search);
        ReportExportService.ExportResult result = exportService.export(reportKey, format, criteria, principal);

        if (result.isAsynchronous()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.of(
                    "Export queued for processing. Poll the export job for its status.",
                    new ExportJobResponse(result.exportJobId())));
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(format.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .body(result.file());
    }

    @GetMapping("/exports/{exportJobId}")
    public ResponseEntity<ApiResponse<ExportStatusResponse>> getExportStatus(
            @PathVariable UUID exportJobId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ExportStatusResponse status = exportService.getExportStatus(exportJobId, principal);
        return ResponseEntity.ok(ApiResponse.of("Export status.", status));
    }
}
