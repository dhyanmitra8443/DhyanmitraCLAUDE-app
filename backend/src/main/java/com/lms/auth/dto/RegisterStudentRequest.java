package com.lms.auth.dto;

import com.lms.shared.validation.FieldsMatch;
import com.lms.shared.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Ref: SRS 3.4. Matches openapi.yaml's RegisterStudentRequest schema. */
@FieldsMatch(field = "password", confirmField = "confirmPassword", message = "Passwords do not match.")
public record RegisterStudentRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @NotBlank String mobileNumber,
        @ValidPassword String password,
        @NotBlank String confirmPassword
) {
}
