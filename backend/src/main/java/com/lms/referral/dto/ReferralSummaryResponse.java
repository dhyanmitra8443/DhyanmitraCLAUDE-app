package com.lms.referral.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A row in the referrer's own "who have I referred" list. {@code status} is the
 * referral lifecycle (PENDING/ACCEPTED/EXPIRED); {@code referredUserStatus} is
 * the joined member's account status once they accept (null while pending).
 */
public record ReferralSummaryResponse(
        UUID id,
        String refereeFirstName,
        String refereeLastName,
        String refereeEmail,
        String status,
        String referredUserStatus,
        OffsetDateTime createdAt,
        OffsetDateTime acceptedAt
) {
}
