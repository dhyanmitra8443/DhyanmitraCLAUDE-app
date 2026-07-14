package com.lms.shared.exception;

/**
 * Thrown when an authenticated user is not permitted to perform an action
 * on a specific resource (role is correct but ownership/assignment isn't -
 * e.g. an instructor editing a course they aren't assigned to). Maps to HTTP 403.
 * Role-level access is instead enforced declaratively via @PreAuthorize.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
