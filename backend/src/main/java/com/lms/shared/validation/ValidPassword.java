package com.lms.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ref: SRS 3.9, 16.5 - password policy. The rules are not fixed here: the
 * validator reads them from system_settings, so an administrator's bounded
 * override (min length 8-64, toggleable complexity categories) takes effect
 * immediately. The message below is only a fallback - the validator
 * replaces it with a description of the policy actually in force.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordConstraintValidator.class)
public @interface ValidPassword {

    String message() default "Password does not meet the configured password policy.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
