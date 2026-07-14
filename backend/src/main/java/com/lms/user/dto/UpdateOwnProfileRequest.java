package com.lms.user.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.util.Set;

/**
 * Matches openapi.yaml's UpdateOwnProfileRequest schema (Ref: SRS 4.6, 4.7).
 * A null field means "leave unchanged" (PATCH semantics); which fields the
 * server actually applies depends on the target user's role, not every
 * field present here - UserService filters instructor-only fields out for
 * non-instructors.
 */
public record UpdateOwnProfileRequest(
        String firstName,
        String lastName,
        String mobileNumber,
        LocalDate dateOfBirth,
        String gender,
        String professionalBio,
        @PositiveOrZero Integer yearsOfExperience,
        Set<String> specializations
) {
}
