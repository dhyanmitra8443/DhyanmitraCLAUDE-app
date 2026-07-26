package com.lms.referral.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** A logged-in STUDENT or INSTRUCTOR referring a prospective member by name + email. */
public record CreateReferralRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email
) {
}
