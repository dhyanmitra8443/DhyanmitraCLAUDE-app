package com.lms.auth.dto;

import com.lms.shared.validation.FieldsMatch;
import com.lms.shared.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

/** Ref: SRS 3.11. Matches openapi.yaml's /auth/change-password request body. */
@FieldsMatch(field = "newPassword", confirmField = "confirmPassword", message = "Passwords do not match.")
public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @ValidPassword String newPassword,
        @NotBlank String confirmPassword
) {
}
