package com.lms.report.service;

import com.lms.config.security.UserPrincipal;
import com.lms.report.ReportCriteria;
import com.lms.report.ReportKey;
import com.lms.report.dto.ExportStatusResponse;
import com.lms.report.entity.ExportStatus;
import com.lms.report.entity.ReportExport;
import com.lms.report.entity.ReportFormat;
import com.lms.report.export.ReportExporter;
import com.lms.report.repository.ReportExportRepository;
import com.lms.shared.exception.ForbiddenException;
import com.lms.shared.exception.ResourceNotFoundException;
import com.lms.shared.storage.FileStorageService;
import com.lms.shared.storage.SignedUrlService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Ref: SRS 15.9, 15.15 - report exports.
 *
 * Small exports are rendered inline and streamed straight back (200 with the
 * file). Anything larger than ASYNC_ROW_THRESHOLD rows is handed to a
 * background job (202 with a pollable exportJobId), because holding an HTTP
 * connection open while rendering tens of thousands of rows into a PDF is
 * how a report endpoint takes down a web worker pool.
 */
@Service
public class ReportExportService {

    /**
     * Ref: SRS 15.15 - "large exports". The threshold is a judgement call, not
     * an SRS figure: a few thousand rows renders in well under a second, so
     * this is set where the synchronous path stops being comfortably fast
     * rather than where it becomes impossible.
     */
    static final int ASYNC_ROW_THRESHOLD = 2_000;

    private static final Duration DOWNLOAD_URL_TTL = Duration.ofMinutes(5);

    private final ReportService reportService;
    private final ReportQueryService queryService;
    private final ReportExporter exporter;
    private final ReportExportRepository exportRepository;
    private final ReportExportJobRunner jobRunner;
    private final FileStorageService fileStorageService;
    private final SignedUrlService signedUrlService;

    public ReportExportService(
            ReportService reportService,
            ReportQueryService queryService,
            ReportExporter exporter,
            ReportExportRepository exportRepository,
            ReportExportJobRunner jobRunner,
            FileStorageService fileStorageService,
            SignedUrlService signedUrlService
    ) {
        this.reportService = reportService;
        this.queryService = queryService;
        this.exporter = exporter;
        this.exportRepository = exportRepository;
        this.jobRunner = jobRunner;
        this.fileStorageService = fileStorageService;
        this.signedUrlService = signedUrlService;
    }

    /**
     * Either an immediately-rendered file, or the id of a queued job - the
     * controller turns the first into a 200 file stream and the second into
     * a 202. Exactly one of the two is ever non-null.
     */
    public record ExportResult(byte[] file, String filename, UUID exportJobId) {

        public boolean isAsynchronous() {
            return exportJobId != null;
        }
    }

    @Transactional
    public ExportResult export(ReportKey key, ReportFormat format, ReportCriteria criteria, UserPrincipal principal) {
        List<Map<String, Object>> rows = queryService.fetchRows(key, criteria);

        if (rows.size() <= ASYNC_ROW_THRESHOLD) {
            return new ExportResult(exporter.export(key, rows, format), filename(key, format), null);
        }

        ReportExport job = new ReportExport();
        job.setReportKey(key);
        job.setFormat(format);
        job.setRequestedBy(principal.getUserId());
        job.setStatus(ExportStatus.PROCESSING);
        exportRepository.save(job);

        // The rows are re-fetched inside the job rather than handed over here:
        // this method's transaction (and its entity state) ends the moment we
        // return, and the job runs on another thread afterwards.
        jobRunner.run(job.getId(), key, format, criteria);

        return new ExportResult(null, null, job.getId());
    }

    /** Ref: SRS 15.15 - poll for status; a READY job yields a short-lived signed download link. */
    @Transactional(readOnly = true)
    public ExportStatusResponse getExportStatus(UUID exportJobId, UserPrincipal principal) {
        ReportExport job = exportRepository.findById(exportJobId)
                .orElseThrow(() -> new ResourceNotFoundException("Export job not found."));

        // The person who asked for the export is the only person who may have
        // it: an export can contain any data its report exposes, so handing it
        // to another caller would sidestep that report's own role scoping.
        if (!job.getRequestedBy().equals(principal.getUserId())) {
            throw new ForbiddenException("You do not have access to this export.");
        }

        if (job.getStatus() != ExportStatus.READY) {
            return new ExportStatusResponse(job.getStatus(), null, null);
        }

        String token = signedUrlService.issue(job.getId().toString(), DOWNLOAD_URL_TTL);
        return new ExportStatusResponse(
                ExportStatus.READY,
                "/api/v1/report-files/download/" + token,
                DOWNLOAD_URL_TTL.toSeconds());
    }

    /** Resolves a signed download token back into the stored export file. */
    @Transactional(readOnly = true)
    public DownloadableExport consumeDownload(String token) {
        UUID exportJobId = signedUrlService.validate(token)
                .map(UUID::fromString)
                .orElseThrow(() -> new ResourceNotFoundException("Download link is invalid or has expired."));

        ReportExport job = exportRepository.findById(exportJobId)
                .orElseThrow(() -> new ResourceNotFoundException("Export job not found."));
        if (job.getStatus() != ExportStatus.READY || job.getFileReference() == null) {
            throw new ResourceNotFoundException("This export is not ready for download.");
        }

        return new DownloadableExport(
                fileStorageService.loadPrivate(job.getFileReference()),
                filename(job.getReportKey(), job.getFormat()),
                job.getFormat());
    }

    public record DownloadableExport(Resource resource, String filename, ReportFormat format) {
    }

    static String filename(ReportKey key, ReportFormat format) {
        return key.name().toLowerCase() + "." + format.extension();
    }
}
