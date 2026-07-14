/**
 * Ref: SRS Chapter 15 - Reports Management (generic reportKey resource, see
 * openapi.yaml).
 *
 * ReportKey is the catalogue: each of the seventeen reports declares its
 * title, the single role that may run it (SRS 15.3), and its columns - which
 * exports reuse as headers, so an empty result set still produces a valid
 * file. ReportQueryService builds the rows with the caller's identity baked
 * into the criteria, so a student's report can only ever read that student's
 * data (SRS 15.13). Exports below a row threshold stream back inline;
 * larger ones become background jobs polled via GET /reports/exports/{id}
 * (SRS 15.15).
 */
package com.lms.report;
