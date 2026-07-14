package com.lms.payment.dto;

/** Ref: SRS 10.12. Fields needed to launch Razorpay Checkout on the client. */
public record RazorpayOrderResponse(
        String razorpayOrderId,
        String razorpayKeyId,
        long amount,
        String currency
) {
}
