package com.lms.resource.dto;

import com.lms.resource.entity.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Matches openapi.yaml's CreateLessonResourceRequest schema (Ref: SRS 8.6, 8.13 - reused for create and update).
 * externalUrl is required for VIDEO/EXTERNAL_LINK; fileReference (from
 * POST /lesson-resources/upload-url) is required for PDF/IMAGE/AUDIO/ZIP -
 * enforced in the service layer since it depends on resourceType.
 */
public record CreateLessonResourceRequest(
        @NotNull ResourceType resourceType,
        @NotBlank String displayName,
        String description,
        String externalUrl,
        String fileReference,
        Integer displayOrder
) {
}
