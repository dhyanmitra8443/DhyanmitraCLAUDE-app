import "server-only";

import { fetchFromBackend } from "@/lib/api/server";
import type { Paginated, UserFullProfile, UserProfile, UserRole, UserSummary } from "@/lib/api/types";

/** Ref: SRS 3.11 - the signed-in user's own profile. */
export function getCurrentUser(): Promise<UserProfile> {
  return fetchFromBackend<UserProfile>("/api/v1/users/me");
}

export interface SearchUsersParams {
  page?: number;
  size?: number;
  sort?: string;
  search?: string;
  role?: UserRole;
  status?: "ACTIVE" | "INACTIVE" | "BLOCKED";
  registeredAfter?: string;
  registeredBefore?: string;
}

/** Ref: SRS 4.9, 4.10, 4.11 - administrator-only user search/list/filter. */
export function searchUsers(params: SearchUsersParams): Promise<Paginated<UserSummary>> {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") query.set(key, String(value));
  }
  const qs = query.toString();
  return fetchFromBackend<Paginated<UserSummary>>(`/api/v1/users${qs ? `?${qs}` : ""}`);
}

/** Ref: SRS 4.12 - administrator-only complete user profile. */
export function getUserFullProfile(userId: string): Promise<UserFullProfile> {
  return fetchFromBackend<UserFullProfile>(`/api/v1/users/${userId}`);
}
