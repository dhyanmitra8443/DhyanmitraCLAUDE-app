"use client";

import { api } from "@/lib/api/client";
import { ApiError } from "@/lib/api/errors";
import type { ApiEnvelope, UserSummary } from "@/lib/api/types";

/**
 * Browser-side wrappers for the three /api/auth/* routes that are kept out of
 * the generic BFF proxy (see app/api/auth/*): login sets httpOnly cookies and
 * deliberately returns only the user summary, and logout/register have their
 * own response shapes. Unwraps the envelope the same way lib/api/client.ts
 * does, so callers get ApiError with field-level messages either way.
 */
async function post<T>(path: string, body: unknown): Promise<T> {
  const response = await fetch(path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  const text = await response.text();
  const envelope = (text ? JSON.parse(text) : null) as
    | (ApiEnvelope<T> & { errors?: { field: string; message: string }[] })
    | null;

  if (!response.ok || envelope?.success === false) {
    const fieldErrors: Record<string, string> = {};
    for (const item of envelope?.errors ?? []) {
      fieldErrors[item.field] = item.message;
    }
    throw new ApiError(response.status, envelope?.message ?? "Something went wrong. Please try again.", fieldErrors);
  }

  return (envelope as ApiEnvelope<T>).data;
}

export function signIn(email: string, password: string): Promise<UserSummary> {
  return post<UserSummary>("/api/auth/login", { email, password });
}

export function signOut(): Promise<null> {
  return post<null>("/api/auth/logout", {});
}

export interface RegisterPayload {
  firstName: string;
  lastName: string;
  email: string;
  mobileNumber: string;
  password: string;
  confirmPassword: string;
}

export function signUp(payload: RegisterPayload): Promise<UserSummary> {
  return post<UserSummary>("/api/auth/register", payload);
}

/** Ref: SRS 3.11 - goes through the generic BFF proxy; no cookies to set. */
export function changePassword(payload: {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}): Promise<null> {
  return api.post<null>("/auth/change-password", payload);
}

/** Ref: SRS 3.5, 4.5 - administrator-only. */
export function inviteInstructor(payload: {
  email: string;
  firstName: string;
  lastName: string;
}): Promise<null> {
  return api.post<null>("/auth/instructors/invitations", payload);
}

/** Ref: SRS 3.5 - public; previews who an invitation token belongs to before asking for a password. */
export function previewInvitation(token: string): Promise<{ email: string; firstName: string; lastName: string }> {
  return api.get(`/auth/instructors/invitations/${encodeURIComponent(token)}`);
}

/** Ref: SRS 3.5 - public; completes instructor onboarding and activates the account. */
export function acceptInvitation(payload: {
  token: string;
  mobileNumber: string;
  password: string;
  confirmPassword: string;
}): Promise<UserSummary> {
  return api.post<UserSummary>("/auth/instructors/accept-invitation", payload);
}
