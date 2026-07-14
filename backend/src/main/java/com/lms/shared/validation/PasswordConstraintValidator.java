package com.lms.shared.validation;

import com.lms.settings.PasswordPolicy;
import com.lms.settings.service.SystemSettingsService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Ref: SRS 3.9, 16.5 - enforces the password policy that is in force *right
 * now*, not a compiled-in one: the rules come from system_settings, so an
 * administrator's PATCH /settings/authentication applies to the very next
 * registration or password reset.
 *
 * The SRS 3.9 defaults (8 chars, upper + lower + digit + special) are what
 * the migration seeds, and SystemSettingsService refuses any override that
 * would drop below the 16.5 floor - so a weaker-than-intended policy cannot
 * be configured here.
 *
 * The violation message is rebuilt from the active policy, otherwise a user
 * told "must include a special character" under a policy that no longer
 * requires one would be chasing a rule that isn't being applied.
 */
public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    private final SystemSettingsService settingsService;

    public PasswordConstraintValidator(SystemSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        PasswordPolicy policy = settingsService.passwordPolicy();
        if (policy.isSatisfiedBy(value)) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(policy.describe()).addConstraintViolation();
        return false;
    }
}
