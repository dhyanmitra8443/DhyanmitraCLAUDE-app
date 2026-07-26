package com.lms.referral.dto;

/** Public preview of a referral token, shown before the referee sets a password. */
public record ReferralPreviewResponse(
        String refereeFirstName,
        String refereeLastName,
        String refereeEmail,
        String referrerName
) {
}
