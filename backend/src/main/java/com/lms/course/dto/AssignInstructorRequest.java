package com.lms.course.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Ref: SRS 5.6. */
public record AssignInstructorRequest(
        @NotNull UUID instructorId
) {
}
