package com.lms.user.dto;

import jakarta.validation.constraints.NotNull;

/** Ref: SRS 4.4, 4.5. Administrators cannot delete users, only change status. */
public record UpdateUserStatusRequest(
        @NotNull String status
) {
}
