package com.lms.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Ref: SRS 3.7, 3.14 - one row per login; the DB's partial unique index
 * (user_id WHERE status = 'ACTIVE') enforces single-active-session, so the
 * service layer must deactivate any existing ACTIVE session before creating
 * a new one rather than relying on this entity to catch the conflict.
 *
 * No updated_at column exists on user_sessions (unlike BaseEntity-mapped
 * tables), so this does not extend BaseEntity.
 */
@Getter
@Setter
@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "device_info")
    private String deviceInfo;

    // A plain String bind against an inet column is rejected by Postgres
    // ("column ip_address is of type inet but expression is of type
    // character varying"); ColumnTransformer makes the generated SQL cast
    // the bound text parameter explicitly instead of relying on JDBC type
    // inference (which mis-binds for OTHER/String combinations in Hibernate 6).
    @ColumnTransformer(write = "?::inet")
    @Column(name = "ip_address", columnDefinition = "inet")
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "login_at", insertable = false, updatable = false)
    private OffsetDateTime loginAt;

    @Column(name = "logout_at")
    private OffsetDateTime logoutAt;
}
