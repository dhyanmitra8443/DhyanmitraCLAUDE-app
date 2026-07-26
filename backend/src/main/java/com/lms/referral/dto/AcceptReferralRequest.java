package com.lms.referral.dto;

import com.lms.shared.validation.FieldsMatch;
import com.lms.shared.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

/** The referee completing sign-up from a referral link; mirrors AcceptInvitationRequest. */
@FieldsMatch(field = "password", confirmField = "confirmPassword", message = "Passwords do not match.")
public record AcceptReferralRequest(
        @NotBlank String token,
        @NotBlank String mobileNumber,
        @ValidPassword String password,
        @NotBlank String confirmPassword
) {
}
