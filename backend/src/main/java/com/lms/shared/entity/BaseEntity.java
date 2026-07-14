package com.lms.shared.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.generator.EventType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Ref: SRS 2.6 - all primary keys are UUIDs.
 *
 * created_at/updated_at are owned by the database (DEFAULT now() and the
 * set_updated_at() trigger from the Flyway migration are the single source
 * of truth), so these fields are read-only from the JPA side
 * (insertable/updatable = false) - Hibernate never writes them, it only
 * reads back whatever the trigger/default produced. This avoids clock-skew
 * or drift between the application server and the database.
 *
 * @Generated tells Hibernate to re-SELECT these columns immediately after
 * INSERT/UPDATE; without it, insertable/updatable=false alone means the
 * in-memory entity keeps them null right after save() until the row is
 * separately reloaded from the DB - a real problem for any create-then-
 * return-the-DTO-in-the-same-response flow (e.g. POST /courses).
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}
