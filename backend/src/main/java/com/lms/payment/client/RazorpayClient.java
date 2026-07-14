package com.lms.payment.client;

/**
 * Ref: SRS 10.12 - "Calls the Razorpay Orders API server-side." Interface
 * boundary so the real HTTP-calling implementation (RazorpayApiClient) can
 * be swapped for a test double without touching OrderService.
 */
public interface RazorpayClient {

    RazorpayOrder createOrder(long amountInPaise, String currency, String receipt);

    record RazorpayOrder(String id, long amount, String currency) {
    }
}
