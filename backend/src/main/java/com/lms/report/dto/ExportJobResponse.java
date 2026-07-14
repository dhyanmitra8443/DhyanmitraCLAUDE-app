package com.lms.report.dto;

import java.util.UUID;

/** Ref: SRS 15.15 - returned with 202 when an export is too large to stream synchronously. */
public record ExportJobResponse(
        UUID exportJobId
) {
}
