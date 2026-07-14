package com.lms.dashboard.controller;

import com.lms.config.security.UserPrincipal;
import com.lms.dashboard.dto.AdminDashboardResponse;
import com.lms.dashboard.dto.InstructorDashboardResponse;
import com.lms.dashboard.dto.StudentDashboardResponse;
import com.lms.dashboard.service.DashboardService;
import com.lms.shared.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Ref: SRS Chapter 13 - Dashboard & Analytics. Matches openapi.yaml's Dashboard & Analytics tag. */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard() {
        return ResponseEntity.ok(ApiResponse.of("Administrator dashboard KPIs and recent activity.", dashboardService.getAdminDashboard()));
    }

    @GetMapping("/instructor")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<InstructorDashboardResponse>> getInstructorDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of("Instructor dashboard KPIs and course summaries.", dashboardService.getInstructorDashboard(principal.getUserId())));
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> getStudentDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of("Student dashboard KPIs, continue-learning, and upcoming live classes.", dashboardService.getStudentDashboard(principal.getUserId())));
    }
}
