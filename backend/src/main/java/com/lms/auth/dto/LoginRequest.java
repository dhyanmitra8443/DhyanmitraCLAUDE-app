package com.lms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Ref: SRS 3.6. */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
