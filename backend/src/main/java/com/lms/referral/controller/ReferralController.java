package com.lms.referral.controller;

import com.lms.config.security.UserPrincipal;
import com.lms.referral.dto.AcceptReferralRequest;
import com.lms.referral.dto.AdminReferralResponse;
import com.lms.referral.dto.CreateReferralRequest;
import com.lms.referral.dto.ReferralPreviewResponse;
import com.lms.referral.dto.ReferralSummaryResponse;
import com.lms.referral.entity.ReferralStatus;
import com.lms.referral.service.ReferralService;
import com.lms.shared.response.ApiResponse;
import com.lms.shared.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Member referrals. Referrers are STUDENT/INSTRUCTOR; accepted accounts are always STUDENT. */
@RestController
@RequestMapping("/api/v1/referrals")
public class ReferralController {

    private final ReferralService referralService;

    public ReferralController(ReferralService referralService) {
        this.referralService = referralService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> createReferral(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateReferralRequest request
    ) {
        referralService.createReferral(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Referral created and invitation email queued."));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<PageResponse<ReferralSummaryResponse>>> listMyReferrals(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<ReferralSummaryResponse> results = referralService.listMyReferrals(principal.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.of("Your referrals, newest first.", results));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<PageResponse<AdminReferralResponse>>> listAllReferrals(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ReferralStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<AdminReferralResponse> results = referralService.listAllReferrals(search, status, page, size);
        return ResponseEntity.ok(ApiResponse.of("All referrals, newest first.", results));
    }

    @GetMapping("/token/{token}")
    public ResponseEntity<ApiResponse<ReferralPreviewResponse>> validateReferral(@PathVariable String token) {
        ReferralPreviewResponse preview = referralService.validateReferral(token);
        return ResponseEntity.ok(ApiResponse.of("Token is valid.", preview));
    }

    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<Void>> acceptReferral(@Valid @RequestBody AcceptReferralRequest request) {
        referralService.acceptReferral(request);
        return ResponseEntity.ok(ApiResponse.of("Account created. You can now sign in."));
    }
}
