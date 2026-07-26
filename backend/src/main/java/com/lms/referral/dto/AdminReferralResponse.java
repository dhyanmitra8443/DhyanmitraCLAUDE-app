package com.lms.referral.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A row in the administrator's cross-referrer view: every referral with both
 * the referrer and the referee resolved, plus the joined member's status.
 */
public record AdminReferralResponse(
        UUID id,
        UUID referrerId,
        String referrerName,
        String referrerEmail,
        String refereeFirstName,
        String refereeLastName,
        String refereeEmail,
        String status,
        UUID referredUserId,
        String referredUserStatus,
        OffsetDateTime createdAt,
        OffsetDateTime acceptedAt
) {
}
