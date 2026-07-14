package com.lms.shared.exception;

/**
 * Thrown when a request is well-formed but violates a documented SRS
 * business rule (e.g. archiving a lesson's only active video resource -
 * Ref: SRS 8.14). Maps to HTTP 409, distinct from validation errors (400).
 */
public class BusinessRuleViolationException extends RuntimeException {
    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
