package com.lms.settings.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Ref: SRS 16.5. passwordMinLength is bounded here at the DTO edge (8-64);
 * the complexity floor - at least one letter class AND one
 * digit-or-special requirement must stay mandatory - depends on the merged
 * result of this partial update, so it is enforced in SystemSettingsService
 * (and again by a CHECK constraint in the database).
 */
public record AuthenticationSettingsRequest(
        @Min(1) Integer sessionTimeoutMinutes,
        @Min(1) Integer maxLoginAttempts,
        @Min(8) @Max(64) Integer passwordMinLength,
        @Valid PasswordComplexityRequest passwordComplexity
) {

    public record PasswordComplexityRequest(
            Boolean requireUppercase,
            Boolean requireLowercase,
            Boolean requireDigit,
            Boolean requireSpecialChar
    ) {
    }
}
