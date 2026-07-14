package com.lms.auth.dto;

import com.lms.shared.validation.FieldsMatch;
import com.lms.shared.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

/** Ref: SRS 3.5. Matches openapi.yaml's /auth/instructors/accept-invitation request body. */
@FieldsMatch(field = "password", confirmField = "confirmPassword", message = "Passwords do not match.")
public record AcceptInvitationRequest(
        @NotBlank String token,
        @ValidPassword String password,
        @NotBlank String confirmPassword
) {
}
