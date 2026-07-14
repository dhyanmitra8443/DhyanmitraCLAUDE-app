package com.lms.resource.dto;

/** Ref: SRS 8.8, 8.15. */
public record UploadUrlResponse(
        String uploadUrl,
        String fileReference,
        long expiresInSeconds
) {
}
