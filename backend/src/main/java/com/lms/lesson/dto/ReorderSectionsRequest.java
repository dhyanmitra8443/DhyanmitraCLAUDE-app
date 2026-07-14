package com.lms.lesson.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/** Ref: SRS 7.10. */
public record ReorderSectionsRequest(
        @NotEmpty List<UUID> sectionIdsInOrder
) {
}
