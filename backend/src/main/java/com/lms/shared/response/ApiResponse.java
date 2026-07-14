package com.lms.shared.response;

/**
 * Standard success response envelope (Ref: SRS 2.8).
 * Every controller returns this shape - see openapi.yaml's ApiEnvelope schema.
 */
public record ApiResponse<T>(boolean success, String message, T data) {

    public static <T> ApiResponse<T> of(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<Void> of(String message) {
        return new ApiResponse<>(true, message, null);
    }
}
