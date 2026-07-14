package com.lms.subscription.controller;

import com.lms.config.security.UserPrincipal;
import com.lms.shared.response.ApiResponse;
import com.lms.subscription.dto.CreateSubscriptionPlanRequest;
import com.lms.subscription.dto.SubscriptionPlanSummaryResponse;
import com.lms.subscription.dto.UpdatePlanStatusRequest;
import com.lms.subscription.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Ref: SRS Chapter 9 - Subscription Plans & Student Enrollments (plans). Matches openapi.yaml's Subscriptions tag. */
@RestController
@RequestMapping("/api/v1")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    public SubscriptionPlanController(SubscriptionPlanService subscriptionPlanService) {
        this.subscriptionPlanService = subscriptionPlanService;
    }

    @GetMapping("/courses/{courseId}/subscription-plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanSummaryResponse>>> listPlans(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = principal != null && principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR"));
        return ResponseEntity.ok(ApiResponse.of("Subscription plans for the course.", subscriptionPlanService.listPlans(courseId, isAdmin)));
    }

    @PostMapping("/courses/{courseId}/subscription-plans")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<SubscriptionPlanSummaryResponse>> createPlan(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateSubscriptionPlanRequest request
    ) {
        SubscriptionPlanSummaryResponse created = subscriptionPlanService.createPlan(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Plan created as ACTIVE.", created));
    }

    @PatchMapping("/subscription-plans/{planId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> updatePlan(
            @PathVariable UUID planId,
            @Valid @RequestBody CreateSubscriptionPlanRequest request
    ) {
        subscriptionPlanService.updatePlan(planId, request);
        return ResponseEntity.ok(ApiResponse.of("Plan updated."));
    }

    @PatchMapping("/subscription-plans/{planId}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable UUID planId,
            @Valid @RequestBody UpdatePlanStatusRequest request
    ) {
        subscriptionPlanService.updateStatus(planId, request.status());
        return ResponseEntity.ok(ApiResponse.of("Status updated."));
    }
}
