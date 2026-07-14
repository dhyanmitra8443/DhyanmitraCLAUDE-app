package com.lms.subscription.controller;

import com.lms.config.security.UserPrincipal;
import com.lms.shared.response.ApiResponse;
import com.lms.shared.response.PageResponse;
import com.lms.subscription.dto.AdminUpdateSubscriptionRequest;
import com.lms.subscription.dto.SubscriptionSummaryResponse;
import com.lms.subscription.entity.SubscriptionStatus;
import com.lms.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Ref: SRS Chapter 9 - Subscription Plans & Student Enrollments (subscriptions). Matches openapi.yaml's Subscriptions tag. */
@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<SubscriptionSummaryResponse>>> getOwnSubscriptions(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of("Student's subscriptions.", subscriptionService.getOwnSubscriptions(principal.getUserId())));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<PageResponse<SubscriptionSummaryResponse>>> searchSubscriptions(
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) SubscriptionStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<SubscriptionSummaryResponse> results = subscriptionService.searchSubscriptions(studentName, courseId, status, pageable);
        return ResponseEntity.ok(ApiResponse.of("Paginated subscription list.", results));
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<ApiResponse<SubscriptionSummaryResponse>> getSubscriptionDetail(
            @PathVariable UUID subscriptionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.of("Subscription details, including full history.", subscriptionService.getSubscriptionDetail(subscriptionId, principal)));
    }

    @PatchMapping("/{subscriptionId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> adminUpdateSubscription(
            @PathVariable UUID subscriptionId,
            @Valid @RequestBody AdminUpdateSubscriptionRequest request
    ) {
        subscriptionService.adminUpdateSubscription(subscriptionId, request);
        return ResponseEntity.ok(ApiResponse.of("Subscription updated."));
    }
}
