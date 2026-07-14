package com.lms.report.controller;

import com.lms.report.service.ReportExportService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ref: SRS 15.15 - the actual byte transfer that the downloadUrl from
 * GET /reports/exports/{exportJobId} points at. Not part of openapi.yaml's
 * documented operations, matching the identical pattern already used for
 * certificate and lesson-resource downloads.
 *
 * Authenticated by the signed, time-limited token in the path rather than a
 * JWT (SignedUrlService), which is why SecurityConfig lists it as public:
 * the token is only ever issued to the caller who requested the export, and
 * it expires in five minutes.
 */
@RestController
@RequestMapping("/api/v1")
public class ReportFileController {

    private final ReportExportService exportService;

    public ReportFileController(ReportExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/report-files/download/{token}")
    public ResponseEntity<Resource> consumeDownload(@PathVariable String token) {
        ReportExportService.DownloadableExport export = exportService.consumeDownload(token);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(export.format().contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(export.filename()).build().toString())
                .body(export.resource());
    }
}
