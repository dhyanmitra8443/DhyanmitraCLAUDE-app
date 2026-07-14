package com.lms.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Ref: SRS 3.10, 3.12 - unified, single-use REFRESH / PASSWORD_RESET tokens.
 * Only token_hash (SHA-256 of the opaque value handed to the client) is
 * persisted, never the raw token - Ref: SRS 17.9.
 *
 * No updated_at column exists on auth_tokens, so this does not extend
 * BaseEntity.
 */
@Getter
@Setter
@Entity
@Table(name = "auth_tokens")
public class AuthToken {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    /** Set for REFRESH tokens only; null for PASSWORD_RESET. */
    @Column(name = "session_id")
    private UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, updatable = false)
    private AuthTokenType tokenType;

    @Column(name = "token_hash", nullable = false, updatable = false)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
