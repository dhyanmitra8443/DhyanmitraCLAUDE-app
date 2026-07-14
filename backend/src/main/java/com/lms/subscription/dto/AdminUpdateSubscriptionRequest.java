package com.lms.subscription.dto;

import java.time.LocalDate;

/** Ref: SRS 9.14 - "Activate a pending subscription, extend it, or correct administrative errors." Both fields optional. */
public record AdminUpdateSubscriptionRequest(
        String status,
        LocalDate endDate
) {
}
