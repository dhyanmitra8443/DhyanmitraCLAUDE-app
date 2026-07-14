package com.lms.shared.exception;

/** Thrown when a requested entity (by ID) does not exist. Maps to HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
