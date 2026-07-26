"use client";

import { api } from "@/lib/api/client";
import type { CreateReferralRequest, ReferralPreview } from "@/lib/api/types";

/** A logged-in STUDENT/INSTRUCTOR refers a prospective member. */
export function createReferral(payload: CreateReferralRequest): Promise<null> {
  return api.post<null>("/referrals", payload);
}

/** Public - previews who a referral token belongs to before asking for a password. */
export function previewReferral(token: string): Promise<ReferralPreview> {
  return api.get<ReferralPreview>(`/referrals/token/${encodeURIComponent(token)}`);
}

/** Public - completes the referee's sign-up and creates their STUDENT account. */
export function acceptReferral(payload: {
  token: string;
  mobileNumber: string;
  password: string;
  confirmPassword: string;
}): Promise<null> {
  return api.post<null>("/referrals/accept", payload);
}
