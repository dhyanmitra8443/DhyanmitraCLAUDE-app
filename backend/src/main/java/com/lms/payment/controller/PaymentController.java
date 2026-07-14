package com.lms.payment.controller;

import com.lms.config.security.UserPrincipal;
import com.lms.payment.dto.PaymentSummaryResponse;
import com.lms.payment.entity.PaymentStatus;
import com.lms.payment.service.PaymentService;
import com.lms.payment.service.RazorpayWebhookService;
import com.lms.shared.response.ApiResponse;
import com.lms.shared.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Ref: SRS Chapter 10 - Payment Management (payments + webhook). Matches openapi.yaml's Payments tag. */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final RazorpayWebhookService razorpayWebhookService;

    public PaymentController(PaymentService paymentService, RazorpayWebhookService razorpayWebhookService) {
        this.paymentService = paymentService;
        this.razorpayWebhookService = razorpayWebhookService;
    }

    @PostMapping("/razorpay/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature,
            @RequestHeader(value = "X-Razorpay-Event-Id", required = false) String eventId
    ) {
        razorpayWebhookService.processWebhook(rawBody, signature, eventId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<PageResponse<PaymentSummaryResponse>>> getOwnPayments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.of("Paginated payment history.", paymentService.getOwnPayments(principal.getUserId(), pageable)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<PageResponse<PaymentSummaryResponse>>> searchPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String transactionReference,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<PaymentSummaryResponse> results = paymentService.searchPayments(status, transactionReference, pageable);
        return ResponseEntity.ok(ApiResponse.of("Paginated payment list.", results));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentSummaryResponse>> getPaymentDetail(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.of("Payment details.", paymentService.getPaymentDetail(paymentId, principal)));
    }
}
