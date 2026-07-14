package com.lms.payment.controller;

import com.lms.config.security.UserPrincipal;
import com.lms.payment.dto.CreateOrderRequest;
import com.lms.payment.dto.OrderSummaryResponse;
import com.lms.payment.dto.RazorpayOrderResponse;
import com.lms.payment.entity.OrderStatus;
import com.lms.payment.service.OrderService;
import com.lms.shared.response.ApiResponse;
import com.lms.shared.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import java.util.UUID;

/** Ref: SRS Chapter 10 - Payment Management (orders). Matches openapi.yaml's Payments tag. */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<OrderSummaryResponse>> createOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        OrderSummaryResponse created = orderService.createOrder(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Order created as PENDING.", created));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> searchOrders(
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<OrderSummaryResponse> results = orderService.searchOrders(studentName, status, pageable);
        return ResponseEntity.ok(ApiResponse.of("Paginated order list (administrator view - all students).", results));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> getOwnOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.of("Paginated order history.", orderService.getOwnOrders(principal.getUserId(), pageable)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderSummaryResponse>> getOrderDetail(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.of("Order details.", orderService.getOrderDetail(orderId, principal)));
    }

    @PostMapping("/{orderId}/razorpay-order")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createRazorpayOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RazorpayOrderResponse created = orderService.createRazorpayOrder(orderId, principal);
        return ResponseEntity.ok(ApiResponse.of("Razorpay order created; use these fields to launch Checkout.", created));
    }
}
