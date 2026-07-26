package com.lms.referral.entity;

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
 * A member referral. Modelled after {@code InstructorInvitation}: single-use,
 * expiring, only the token_hash is persisted (never the raw token handed to the
 * referee). Adds referrer_id (who referred) and referred_user_id (the STUDENT
 * account created on acceptance, which powers the "current status" column).
 *
 * No updated_at column exists on referrals, so this does not extend BaseEntity.
 */
@Getter
@Setter
@Entity
@Table(name = "referrals")
public class Referral {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "referrer_id", nullable = false, updatable = false)
    private UUID referrerId;

    @Column(name = "referee_email", nullable = false, updatable = false, columnDefinition = "citext")
    private String refereeEmail;

    @Column(name = "referee_first_name", nullable = false, updatable = false)
    private String refereeFirstName;

    @Column(name = "referee_last_name", nullable = false, updatable = false)
    private String refereeLastName;

    @Column(name = "token_hash", nullable = false, updatable = false)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferralStatus status = ReferralStatus.PENDING;

    @Column(name = "referred_user_id")
    private UUID referredUserId;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
