package com.lms.lesson.dto;

import jakarta.validation.constraints.NotNull;

/** Ref: SRS 7.11. */
public record SetPreviewRequest(
        @NotNull Boolean isPreview
) {
}
