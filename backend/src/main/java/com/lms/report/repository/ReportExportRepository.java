package com.lms.report.repository;

import com.lms.report.entity.ReportExport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Ref: SRS 15.15 - asynchronous export job tracking. */
public interface ReportExportRepository extends JpaRepository<ReportExport, UUID> {
}
