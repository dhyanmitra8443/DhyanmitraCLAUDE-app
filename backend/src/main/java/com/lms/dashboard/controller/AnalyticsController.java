package com.lms.dashboard.controller;

import com.lms.dashboard.service.AnalyticsService;
import com.lms.dashboard.service.AnalyticsType;
import com.lms.shared.response.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

/** Ref: SRS 13.9 - Administrator-only named analytics summaries. */
@RestController
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/api/v1/analytics/{analyticsType}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics(
            @PathVariable String analyticsType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        AnalyticsType type = AnalyticsType.fromPath(analyticsType);
        return ResponseEntity.ok(ApiResponse.of("Analytics summary.", analyticsService.getAnalytics(type, dateFrom, dateTo)));
    }
}
