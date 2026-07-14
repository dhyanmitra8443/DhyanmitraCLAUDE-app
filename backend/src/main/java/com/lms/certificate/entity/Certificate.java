package com.lms.certificate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Ref: SRS 12.11 - immutable after issuance (DB trigger blocks
 * UPDATE/DELETE outright); organizationNameSnapshot/instructorNamesSnapshot
 * are captured at issuance so later org/instructor changes never alter an
 * already-issued certificate. No updated_at column exists, so this does
 * not extend BaseEntity.
 */
@Getter
@Setter
@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "certificate_number", nullable = false, updatable = false)
    private String certificateNumber;

    @Column(name = "verification_id", nullable = false, updatable = false)
    private UUID verificationId;

    @Column(name = "student_id", nullable = false, updatable = false)
    private UUID studentId;

    @Column(name = "course_id", nullable = false, updatable = false)
    private UUID courseId;

    @Column(name = "completion_date", nullable = false, updatable = false)
    private LocalDate completionDate;

    @Column(name = "issue_date", nullable = false, updatable = false)
    private LocalDate issueDate;

    @Column(name = "organization_name_snapshot", nullable = false, updatable = false)
    private String organizationNameSnapshot;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "instructor_names_snapshot", nullable = false, updatable = false)
    private String[] instructorNamesSnapshot;

    @Column(name = "file_reference", updatable = false)
    private String fileReference;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
