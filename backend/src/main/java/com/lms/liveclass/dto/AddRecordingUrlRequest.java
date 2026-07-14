package com.lms.liveclass.dto;

import jakarta.validation.constraints.NotBlank;

/** Ref: SRS 11.11. */
public record AddRecordingUrlRequest(
        @NotBlank String recordingUrl
) {
}
