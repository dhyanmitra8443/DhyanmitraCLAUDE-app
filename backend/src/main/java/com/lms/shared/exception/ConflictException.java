package com.lms.shared.exception;

/**
 * Thrown for uniqueness/state conflicts (e.g. duplicate email, publishing a
 * course without required prerequisites). Maps to HTTP 409.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
