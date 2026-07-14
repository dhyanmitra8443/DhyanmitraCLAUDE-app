package com.lms.liveclass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Ref: SRS 11.9 - one row per student per live class (UNIQUE(live_class_id,
 * student_id)); joining twice is treated as idempotent, not an error.
 * No updated_at column exists, so this does not extend BaseEntity.
 */
@Getter
@Setter
@Entity
@Table(name = "live_class_attendance")
public class LiveClassAttendance {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "live_class_id", nullable = false, updatable = false)
    private UUID liveClassId;

    @Column(name = "student_id", nullable = false, updatable = false)
    private UUID studentId;

    @Generated(event = EventType.INSERT)
    @Column(name = "joined_at", insertable = false, updatable = false)
    private OffsetDateTime joinedAt;
}
