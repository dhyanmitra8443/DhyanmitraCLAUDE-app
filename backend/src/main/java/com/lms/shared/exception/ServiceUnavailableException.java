package com.lms.shared.exception;

/** Thrown when a required external integration (e.g. Razorpay) isn't configured or is unreachable. Maps to HTTP 503. */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
