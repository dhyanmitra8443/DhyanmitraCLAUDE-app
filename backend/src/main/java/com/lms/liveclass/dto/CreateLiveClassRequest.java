package com.lms.liveclass.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/** Matches openapi.yaml's CreateLiveClassRequest schema (Ref: SRS 11.6, 11.7, 11.12 - reused for create and update). */
public record CreateLiveClassRequest(
        @NotBlank String title,
        String description,
        @NotNull LocalDate scheduledDate,
        @NotNull LocalTime scheduledTime,
        @NotBlank String meetingUrl,
        String meetingPassword
) {
}
