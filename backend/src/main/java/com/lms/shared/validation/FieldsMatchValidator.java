package com.lms.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

/**
 * Applies to record DTOs, so field access goes through the record's own
 * no-arg accessor (e.g. password()) rather than JavaBean getX() naming -
 * BeanWrapperImpl does not recognize record accessors, hence plain
 * reflection here instead.
 */
public class FieldsMatchValidator implements ConstraintValidator<FieldsMatch, Object> {

    private String field;
    private String confirmField;
    private String message;

    @Override
    public void initialize(FieldsMatch annotation) {
        this.field = annotation.field();
        this.confirmField = annotation.confirmField();
        this.message = annotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        Object fieldValue = invokeAccessor(value, field);
        Object confirmValue = invokeAccessor(value, confirmField);
        if (Objects.equals(fieldValue, confirmValue)) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(confirmField)
                .addConstraintViolation();
        return false;
    }

    private Object invokeAccessor(Object value, String accessorName) {
        try {
            return value.getClass().getMethod(accessorName).invoke(value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "FieldsMatch: no accessor '" + accessorName + "' on " + value.getClass(), e);
        }
    }
}
