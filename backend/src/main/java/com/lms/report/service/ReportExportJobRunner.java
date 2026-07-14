package com.lms.report.service;

import com.lms.report.ReportCriteria;
import com.lms.report.ReportKey;
import com.lms.report.entity.ExportStatus;
import com.lms.report.entity.ReportExport;
import com.lms.report.entity.ReportFormat;
import com.lms.report.export.ReportExporter;
import com.lms.report.repository.ReportExportRepository;
import com.lms.shared.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Ref: SRS 15.15 - runs a large export off the request thread.
 *
 * This lives in its own bean rather than as an @Async method on
 * ReportExportService because Spring's @Async is proxy-based: a service
 * calling its own @Async method would run it inline on the caller's thread,
 * silently defeating the whole point.
 */
@Component
public class ReportExportJobRunner {

    private static final Logger log = LoggerFactory.getLogger(ReportExportJobRunner.class);

    private final ReportQueryService queryService;
    private final ReportExporter exporter;
    private final ReportExportRepository exportRepository;
    private final FileStorageService fileStorageService;

    public ReportExportJobRunner(
            ReportQueryService queryService,
            ReportExporter exporter,
            ReportExportRepository exportRepository,
            FileStorageService fileStorageService
    ) {
        this.queryService = queryService;
        this.exporter = exporter;
        this.exportRepository = exportRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Deliberately not @Transactional as a whole: if the rendering failed
     * partway through a surrounding transaction, that transaction would be
     * marked rollback-only and the FAILED status written in the catch block
     * would be rolled back with it - leaving the job stuck on PROCESSING,
     * which is precisely the state the catch exists to prevent. The read
     * (fetchRows) and each save manage their own transactions instead.
     */
    @Async("reportExecutor")
    public void run(UUID exportJobId, ReportKey key, ReportFormat format, ReportCriteria criteria) {
        try {
            List<Map<String, Object>> rows = queryService.fetchRows(key, criteria);
            byte[] file = exporter.export(key, rows, format);

            String reference = "reports/" + exportJobId + "." + format.extension();
            fileStorageService.writePrivate(file, reference);

            ReportExport job = exportRepository.findById(exportJobId).orElseThrow();
            job.setFileReference(reference);
            job.setStatus(ExportStatus.READY);
            job.setCompletedAt(OffsetDateTime.now());
            exportRepository.save(job);

            log.info("Report export {} ready ({} rows, {}).", exportJobId, rows.size(), format);
        } catch (RuntimeException e) {
            // A failed export must leave the job in a terminal FAILED state, not
            // stuck on PROCESSING forever - the client is polling it.
            log.error("Report export {} failed", exportJobId, e);
            exportRepository.findById(exportJobId).ifPresent(job -> {
                job.setStatus(ExportStatus.FAILED);
                job.setCompletedAt(OffsetDateTime.now());
                exportRepository.save(job);
            });
        }
    }
}
