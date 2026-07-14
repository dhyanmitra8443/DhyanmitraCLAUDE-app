package com.lms.settings.dto;

/** Ref: SRS 16.8 - "Disabling a notification channel affects future notifications only." */
public record NotificationSettingsRequest(
        Boolean emailNotificationsEnabled,
        Boolean inAppNotificationsEnabled
) {
}
