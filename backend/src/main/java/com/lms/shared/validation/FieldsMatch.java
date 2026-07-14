package com.lms.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level constraint asserting two named fields hold equal values, e.g.
 * password/confirmPassword pairs (Ref: SRS 3.4, 3.10, 3.11).
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FieldsMatchValidator.class)
public @interface FieldsMatch {

    String field();

    String confirmField();

    String message() default "Values do not match.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
