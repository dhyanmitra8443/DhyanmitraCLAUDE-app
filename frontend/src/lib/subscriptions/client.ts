"use client";

import { api } from "@/lib/api/client";
import type { CreateSubscriptionPlanRequest, SubscriptionPlanSummary, SubscriptionStatus } from "@/lib/api/types";

/** Ref: SRS 9.3, 9.15 - administrator-only. Defaults to ACTIVE. */
export function createSubscriptionPlan(
  courseId: string,
  payload: CreateSubscriptionPlanRequest,
): Promise<SubscriptionPlanSummary> {
  return api.post<SubscriptionPlanSummary>(`/courses/${courseId}/subscription-plans`, payload);
}

/** Ref: SRS 9.15 - reuses the create DTO (full replace); does not affect subscriptions already purchased. */
export function updateSubscriptionPlan(
  planId: string,
  payload: CreateSubscriptionPlanRequest,
): Promise<null> {
  return api.patch<null>(`/subscription-plans/${planId}`, payload);
}

/** Ref: SRS 9.5, 9.15 - activate/deactivate/archive. */
export function updatePlanStatus(planId: string, status: "ACTIVE" | "INACTIVE" | "ARCHIVED"): Promise<null> {
  return api.patch<null>(`/subscription-plans/${planId}/status`, { status });
}

/**
 * Ref: SRS 9.14 - administrator-only. A raw overwrite (sets endDate/status
 * directly), not duration-aware "add N days" math - that logic only exists
 * inside Ch.10's Razorpay webhook path.
 */
export function adminUpdateSubscription(
  subscriptionId: string,
  payload: { status?: SubscriptionStatus; endDate?: string },
): Promise<null> {
  return api.patch<null>(`/subscriptions/${subscriptionId}`, payload);
}
