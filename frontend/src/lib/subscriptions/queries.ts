import "server-only";

import { fetchFromBackend } from "@/lib/api/server";
import type { Paginated, SubscriptionStatus, SubscriptionSummary, SubscriptionPlanSummary } from "@/lib/api/types";

/** Ref: SRS 9.6 - public callers see only ACTIVE plans; administrators see all. */
export function listSubscriptionPlans(courseId: string): Promise<SubscriptionPlanSummary[]> {
  return fetchFromBackend<SubscriptionPlanSummary[]>(`/api/v1/courses/${courseId}/subscription-plans`);
}

/** Ref: SRS 9.13, 9.18 - the authenticated student's own subscriptions. */
export function getMySubscriptions(): Promise<SubscriptionSummary[]> {
  return fetchFromBackend<SubscriptionSummary[]>("/api/v1/subscriptions/me");
}

export interface SearchSubscriptionsParams {
  page?: number;
  size?: number;
  sort?: string;
  studentName?: string;
  courseId?: string;
  status?: SubscriptionStatus;
}

/** Ref: SRS 9.16 - administrator-only. */
export function searchSubscriptions(params: SearchSubscriptionsParams): Promise<Paginated<SubscriptionSummary>> {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") query.set(key, String(value));
  }
  const qs = query.toString();
  return fetchFromBackend<Paginated<SubscriptionSummary>>(`/api/v1/subscriptions${qs ? `?${qs}` : ""}`);
}

/** Ref: SRS 9.13 - administrator, or the owning student. */
export function getSubscriptionDetail(subscriptionId: string): Promise<SubscriptionSummary> {
  return fetchFromBackend<SubscriptionSummary>(`/api/v1/subscriptions/${subscriptionId}`);
}
