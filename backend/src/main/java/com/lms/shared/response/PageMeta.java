package com.lms.shared.response;

/** Matches openapi.yaml's PageMeta schema, used by every paginated list endpoint. */
public record PageMeta(int page, int size, long totalElements, int totalPages) {
}
