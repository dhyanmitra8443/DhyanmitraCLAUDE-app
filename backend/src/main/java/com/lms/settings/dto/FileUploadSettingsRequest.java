package com.lms.settings.dto;

import jakarta.validation.constraints.Min;

import java.util.List;

/** Ref: SRS 16.6. */
public record FileUploadSettingsRequest(
        @Min(1) Integer maxUploadSizeMb,
        List<String> allowedFileTypes,
        @Min(1) Integer maxFilesPerUpload
) {
}
