package com.lms.shared.response;

import java.util.List;

/**
 * Standard error response envelope (Ref: SRS 2.8, "standardized API error responses").
 * Matches openapi.yaml's ErrorResponse schema exactly.
 */
public record ApiErrorResponse(boolean success, String message, List<FieldErrorItem> errors) {

    public static ApiErrorResponse of(String message) {
        return new ApiErrorResponse(false, message, List.of());
    }

    public static ApiErrorResponse of(String message, List<FieldErrorItem> errors) {
        return new ApiErrorResponse(false, message, errors);
    }
}
