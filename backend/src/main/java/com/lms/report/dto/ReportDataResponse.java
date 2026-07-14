package com.lms.report.dto;

import com.lms.report.ReportKey;
import com.lms.shared.response.PageMeta;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Ref: SRS 15.4-15.11 - report data. The row shape depends on the report, so
 * rows are ordered maps keyed by the report's own column keys rather than a
 * fixed schema (openapi.yaml declares `content: array of object` for exactly
 * this reason).
 *
 * `columns` is included so a client can render headers - and render them in
 * the right order - without hardcoding a column list per report, and without
 * having to guess from the first row when the result set is empty.
 */
public record ReportDataResponse(
        OffsetDateTime generatedAt,
        List<ReportKey.Column> columns,
        List<Map<String, Object>> content,
        PageMeta page
) {
}
