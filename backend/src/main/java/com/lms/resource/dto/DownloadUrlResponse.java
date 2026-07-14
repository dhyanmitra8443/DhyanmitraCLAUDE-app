package com.lms.resource.dto;

/** Ref: SRS 8.12, 17.24. */
public record DownloadUrlResponse(
        String downloadUrl,
        long expiresInSeconds
) {
}
