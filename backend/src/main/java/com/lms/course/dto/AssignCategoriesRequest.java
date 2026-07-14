package com.lms.course.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;
import java.util.UUID;

/** Ref: SRS 5.7, 6.6. */
public record AssignCategoriesRequest(
        @NotEmpty Set<UUID> categoryIds
) {
}
