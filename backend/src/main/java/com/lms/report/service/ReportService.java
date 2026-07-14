package com.lms.report.service;

import com.lms.config.security.UserPrincipal;
import com.lms.report.ReportCriteria;
import com.lms.report.ReportKey;
import com.lms.report.dto.ReportDataResponse;
import com.lms.report.dto.ReportDefinitionResponse;
import com.lms.shared.exception.ForbiddenException;
import com.lms.shared.response.PageMeta;
import com.lms.user.entity.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Ref: SRS Chapter 15 - Reports Management. The entry point for both the
 * data endpoint and the export endpoint: it resolves the caller's role,
 * enforces that the role owns the requested report (SRS 15.3), and builds
 * the ReportCriteria that carries the caller's identity into every query so
 * scoping (SRS 15.13) cannot be bypassed.
 */
@Service
public class ReportService {

    private final ReportQueryService queryService;

    public ReportService(ReportQueryService queryService) {
        this.queryService = queryService;
    }

    /** Ref: SRS 15.3 - only the reports this caller's role may run. */
    public List<ReportDefinitionResponse> listAvailableReports(UserPrincipal principal) {
        return ReportKey.availableTo(roleOf(principal)).stream()
                .map(ReportDefinitionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReportDataResponse getReportData(
            ReportKey key,
            UserPrincipal principal,
            LocalDate dateFrom,
            LocalDate dateTo,
            UUID courseId,
            String search,
            int page,
            int size
    ) {
        ReportCriteria criteria = criteriaFor(key, principal, dateFrom, dateTo, courseId, search);
        List<Map<String, Object>> allRows = queryService.fetchRows(key, criteria);

        int from = Math.min(page * size, allRows.size());
        int to = Math.min(from + size, allRows.size());
        List<Map<String, Object>> pageRows = allRows.subList(from, to);
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) allRows.size() / size);

        return new ReportDataResponse(
                OffsetDateTime.now(),
                key.columns(),
                pageRows,
                new PageMeta(page, size, allRows.size(), totalPages)
        );
    }

    /** Builds the criteria after checking the caller may run this report at all. */
    public ReportCriteria criteriaFor(
            ReportKey key,
            UserPrincipal principal,
            LocalDate dateFrom,
            LocalDate dateTo,
            UUID courseId,
            String search
    ) {
        UserRole role = roleOf(principal);
        assertAvailable(key, role);
        return new ReportCriteria(principal.getUserId(), role, dateFrom, dateTo, courseId, search);
    }

    /**
     * Ref: SRS 15.3 - "reportKey is not available to the caller's role" is a
     * 403, not a 404: the report exists, the caller just may not run it.
     */
    private void assertAvailable(ReportKey key, UserRole role) {
        if (!key.isAvailableTo(role)) {
            throw new ForbiddenException("This report is not available to your role.");
        }
    }

    private UserRole roleOf(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .map(UserRole::valueOf)
                .findFirst()
                .orElseThrow(() -> new ForbiddenException("No role is associated with this account."));
    }
}
