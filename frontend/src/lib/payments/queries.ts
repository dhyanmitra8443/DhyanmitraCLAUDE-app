import "server-only";

import { fetchFromBackend } from "@/lib/api/server";
import type { OrderStatus, OrderSummary, Paginated, PaymentStatus, PaymentSummary } from "@/lib/api/types";

/** Ref: SRS 10.12 - the authenticated student's own order history. */
export function listMyOrders(params: { page?: number; size?: number } = {}): Promise<Paginated<OrderSummary>> {
  const query = new URLSearchParams();
  if (params.page != null) query.set("page", String(params.page));
  if (params.size != null) query.set("size", String(params.size));
  const qs = query.toString();
  return fetchFromBackend<Paginated<OrderSummary>>(`/api/v1/orders/me${qs ? `?${qs}` : ""}`);
}

/** Ref: SRS 10.4 - administrator, or the owning student. */
export function getOrderDetail(orderId: string): Promise<OrderSummary> {
  return fetchFromBackend<OrderSummary>(`/api/v1/orders/${orderId}`);
}

export interface SearchOrdersParams {
  page?: number;
  size?: number;
  sort?: string;
  studentName?: string;
  status?: OrderStatus;
}

/** Ref: SRS 10.15 - administrator-only. */
export function searchOrders(params: SearchOrdersParams): Promise<Paginated<OrderSummary>> {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") query.set(key, String(value));
  }
  const qs = query.toString();
  return fetchFromBackend<Paginated<OrderSummary>>(`/api/v1/orders${qs ? `?${qs}` : ""}`);
}

/** Ref: SRS 10.13 - the authenticated student's own payment history. */
export function listMyPayments(params: { page?: number; size?: number } = {}): Promise<Paginated<PaymentSummary>> {
  const query = new URLSearchParams();
  if (params.page != null) query.set("page", String(params.page));
  if (params.size != null) query.set("size", String(params.size));
  const qs = query.toString();
  return fetchFromBackend<Paginated<PaymentSummary>>(`/api/v1/payments/me${qs ? `?${qs}` : ""}`);
}

/** Ref: SRS 10.6 - administrator, or the owning student. */
export function getPaymentDetail(paymentId: string): Promise<PaymentSummary> {
  return fetchFromBackend<PaymentSummary>(`/api/v1/payments/${paymentId}`);
}

export interface SearchPaymentsParams {
  page?: number;
  size?: number;
  sort?: string;
  status?: PaymentStatus;
  transactionReference?: string;
}

/** Ref: SRS 10.16 - administrator-only. */
export function searchPayments(params: SearchPaymentsParams): Promise<Paginated<PaymentSummary>> {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") query.set(key, String(value));
  }
  const qs = query.toString();
  return fetchFromBackend<Paginated<PaymentSummary>>(`/api/v1/payments${qs ? `?${qs}` : ""}`);
}
