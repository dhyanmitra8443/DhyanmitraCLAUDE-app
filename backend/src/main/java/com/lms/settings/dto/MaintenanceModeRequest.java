package com.lms.settings.dto;

import jakarta.validation.constraints.NotNull;

/** Ref: SRS 16.12 - while enabled, only administrators may access the system. */
public record MaintenanceModeRequest(
        @NotNull Boolean enabled
) {
}
