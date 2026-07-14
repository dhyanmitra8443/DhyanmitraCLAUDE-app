package com.lms.report.entity;

import org.springframework.http.MediaType;

/** Ref: SRS 15.9 - the export formats the contract offers. */
public enum ReportFormat {

    PDF("pdf", MediaType.APPLICATION_PDF_VALUE),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    CSV("csv", "text/csv");

    private final String extension;
    private final String contentType;

    ReportFormat(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public String extension() {
        return extension;
    }

    public String contentType() {
        return contentType;
    }
}
