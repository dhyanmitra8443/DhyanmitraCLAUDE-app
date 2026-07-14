package com.lms.resource.dto;

import jakarta.validation.constraints.NotBlank;

/** Ref: SRS 8.8, 8.15. */
public record UploadUrlRequest(
        @NotBlank String fileName,
        @NotBlank String contentType
) {
}
