package com.lms.lesson.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/** Ref: SRS 7.9. */
public record ReorderLessonsRequest(
        @NotEmpty List<UUID> lessonIdsInOrder
) {
}
