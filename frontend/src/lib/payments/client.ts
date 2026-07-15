"use client";

import { api } from "@/lib/api/client";
import type { CreateOrderRequest, OrderSummary, RazorpayOrderResponse } from "@/lib/api/types";

/** Ref: SRS 10.3, 10.4 - creates a PENDING order for a course + subscription plan. */
export function createOrder(payload: CreateOrderRequest): Promise<OrderSummary> {
  return api.post<OrderSummary>("/orders", payload);
}

/**
 * Ref: SRS 10.12 - client-side poll target after Razorpay Checkout's success
 * callback fires. That callback is explicitly non-authoritative (SRS 10.12:
 * "Payment status shall be finalized only through a verified webhook"), so
 * the UI polls this until the backend's webhook processing has caught up and
 * flipped the order out of PENDING.
 */
export function getOrderStatus(orderId: string): Promise<OrderSummary> {
  return api.get<OrderSummary>(`/orders/${orderId}`);
}

/**
 * Ref: SRS 10.12 - calls the Razorpay Orders API server-side and returns the
 * fields needed to launch Razorpay Checkout on the client. Safe to call again
 * for the same (still-PENDING) order to retry a dismissed/failed checkout
 * attempt - each call creates a fresh Payment row server-side.
 */
export function createRazorpayOrder(orderId: string): Promise<RazorpayOrderResponse> {
  return api.post<RazorpayOrderResponse>(`/orders/${orderId}/razorpay-order`, undefined);
}
