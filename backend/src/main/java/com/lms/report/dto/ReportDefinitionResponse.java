package com.lms.report.dto;

import com.lms.report.ReportKey;

/** Ref: SRS 15.3 - one entry per report the caller's role may run. */
public record ReportDefinitionResponse(
        ReportKey reportKey,
        String title
) {

    public static ReportDefinitionResponse from(ReportKey key) {
        return new ReportDefinitionResponse(key, key.title());
    }
}
