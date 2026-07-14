package com.lms.settings;

/**
 * Ref: SRS 3.9, 16.5 - the password rules actually in force right now.
 * Read by PasswordConstraintValidator on every password-bearing request, so
 * an administrator's PATCH /settings/authentication takes effect
 * immediately rather than at the next deploy.
 *
 * The 3.9 defaults (8 chars, upper + lower + digit + special) are what the
 * migration seeds; SystemSettingsService guarantees any override still
 * keeps at least one letter class and one digit-or-special requirement
 * mandatory, so this can never degrade to "no complexity at all".
 */
public record PasswordPolicy(
        int minLength,
        boolean requireUppercase,
        boolean requireLowercase,
        boolean requireDigit,
        boolean requireSpecialChar
) {

    /** Fallback used only if the settings row cannot be read - the SRS 3.9 defaults, never something weaker. */
    public static PasswordPolicy srsDefault() {
        return new PasswordPolicy(8, true, true, true, true);
    }

    public boolean isSatisfiedBy(String password) {
        if (password == null || password.length() < minLength) {
            return false;
        }
        if (requireUppercase && password.chars().noneMatch(Character::isUpperCase)) {
            return false;
        }
        if (requireLowercase && password.chars().noneMatch(Character::isLowerCase)) {
            return false;
        }
        if (requireDigit && password.chars().noneMatch(Character::isDigit)) {
            return false;
        }
        // "Special" is anything that isn't a letter or a digit - matching the
        // [^A-Za-z0-9] class the original hardcoded 3.9 regex used.
        return !requireSpecialChar || password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
    }

    /** Human-readable rules, used as the validation message so callers know what to fix. */
    public String describe() {
        StringBuilder sb = new StringBuilder("Password must be at least " + minLength + " characters");
        StringBuilder classes = new StringBuilder();
        appendClass(classes, requireUppercase, "an uppercase letter");
        appendClass(classes, requireLowercase, "a lowercase letter");
        appendClass(classes, requireDigit, "a digit");
        appendClass(classes, requireSpecialChar, "a special character");
        if (!classes.isEmpty()) {
            sb.append(" and include ").append(classes);
        }
        return sb.append('.').toString();
    }

    private static void appendClass(StringBuilder sb, boolean required, String label) {
        if (required) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(label);
        }
    }
}
