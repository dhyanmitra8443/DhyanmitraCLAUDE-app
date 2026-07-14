package com.lms.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Ref: SRS 3.12. */
public record RefreshTokenRequest(
        @NotBlank String refreshToken
) {
}
