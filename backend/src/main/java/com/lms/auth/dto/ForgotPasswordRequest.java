package com.lms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Ref: SRS 3.10. */
public record ForgotPasswordRequest(
        @NotBlank @Email String email
) {
}
