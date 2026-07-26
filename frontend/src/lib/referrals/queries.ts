import "server-only";

import { fetchFromBackend } from "@/lib/api/server";
import type { AdminReferral, Paginated, ReferralStatus, ReferralSummary } from "@/lib/api/types";

/** The referrer's own referrals ("who have I referred"). */
export function getMyReferrals(params: { page?: number; size?: number } = {}): Promise<Paginated<ReferralSummary>> {
  const query = new URLSearchParams();
  if (params.page !== undefined) query.set("page", String(params.page));
  if (params.size !== undefined) query.set("size", String(params.size));
  const qs = query.toString();
  return fetchFromBackend<Paginated<ReferralSummary>>(`/api/v1/referrals/mine${qs ? `?${qs}` : ""}`);
}

/** Administrator-only cross-referrer view of every referral. */
export function getAllReferrals(params: {
  page?: number;
  size?: number;
  search?: string;
  status?: ReferralStatus;
}): Promise<Paginated<AdminReferral>> {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") query.set(key, String(value));
  }
  const qs = query.toString();
  return fetchFromBackend<Paginated<AdminReferral>>(`/api/v1/referrals${qs ? `?${qs}` : ""}`);
}
