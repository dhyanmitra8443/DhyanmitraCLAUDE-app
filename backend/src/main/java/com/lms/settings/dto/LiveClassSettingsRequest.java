package com.lms.settings.dto;

import jakarta.validation.constraints.Min;

/** Ref: SRS 16.10 - defaults only; instructors may override these per class. */
public record LiveClassSettingsRequest(
        String defaultTimeZone,
        @Min(1) Integer defaultMeetingDurationMinutes,
        @Min(0) Integer defaultReminderMinutesBefore
) {
}
