package com.lms.shared.response;

/** One entry in ApiErrorResponse.errors() - matches openapi.yaml's ErrorResponse.errors items. */
public record FieldErrorItem(String field, String message) {
}
