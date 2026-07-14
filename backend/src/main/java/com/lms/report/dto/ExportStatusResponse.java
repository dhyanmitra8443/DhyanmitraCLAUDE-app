package com.lms.report.dto;

import com.lms.report.entity.ExportStatus;

/**
 * Ref: SRS 15.15 - poll response. downloadUrl is null until status is READY;
 * when present it is a short-lived signed link, not a permanent one.
 */
public record ExportStatusResponse(
        ExportStatus status,
        String downloadUrl,
        Long expiresInSeconds
) {
}
