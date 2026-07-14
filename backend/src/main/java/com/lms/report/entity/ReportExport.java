package com.lms.report.entity;

import com.lms.report.ReportKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.generator.EventType;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Ref: SRS 15.15 - an asynchronous export job. Only large exports create one
 * (small ones stream the file back directly); this row is what
 * GET /reports/exports/{exportJobId} polls.
 *
 * No updated_at column exists on report_exports, so this does not extend
 * BaseEntity - completedAt is the "finished" timestamp.
 */
@Getter
@Setter
@Entity
@Table(name = "report_exports")
public class ReportExport {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_key", nullable = false, updatable = false)
    private ReportKey reportKey;

    @Column(name = "requested_by", nullable = false, updatable = false)
    private UUID requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private ReportFormat format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExportStatus status = ExportStatus.PROCESSING;

    @Column(name = "file_reference")
    private String fileReference;

    @Generated(event = EventType.INSERT)
    @Column(name = "requested_at", insertable = false, updatable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
}
