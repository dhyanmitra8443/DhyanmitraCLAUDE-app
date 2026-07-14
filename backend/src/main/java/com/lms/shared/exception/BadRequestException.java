package com.lms.shared.exception;

/**
 * Thrown for well-formed-but-semantically-invalid requests that aren't Bean
 * Validation failures (e.g. an expired/used password-reset or invitation
 * token - Ref: SRS 3.10, 3.5). Maps to HTTP 400.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
