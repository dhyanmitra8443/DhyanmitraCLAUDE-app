"use client";

import { api } from "@/lib/api/client";
import type { UpdateOwnProfileRequest, UserFullProfile, UserProfile } from "@/lib/api/types";

/** Ref: SRS 4.6, 4.7 - fields accepted depend on the caller's role; the server ignores the rest. */
export function updateOwnProfile(payload: UpdateOwnProfileRequest): Promise<UserProfile> {
  return api.patch<UserProfile>("/users/me", payload);
}

/** Ref: SRS 4.13 - JPG/PNG only, max 5 MB (enforced server-side). */
export function uploadOwnPhoto(file: File): Promise<{ profilePhotoUrl: string }> {
  const formData = new FormData();
  formData.append("file", file);
  return api.post<{ profilePhotoUrl: string }>("/users/me/photo", formData);
}

/** Ref: SRS 4.5 - administrator editing another user's profile. */
export function adminUpdateUser(userId: string, payload: UpdateOwnProfileRequest): Promise<UserFullProfile> {
  return api.patch<UserFullProfile>(`/users/${userId}`, payload);
}

/** Ref: SRS 4.4, 4.5 - the only status-changing operation; administrators cannot delete users. */
export function updateUserStatus(
  userId: string,
  status: "ACTIVE" | "INACTIVE" | "BLOCKED",
): Promise<null> {
  return api.patch<null>(`/users/${userId}/status`, { status });
}
