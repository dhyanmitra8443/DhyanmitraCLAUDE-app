import type { UserRole } from "@/lib/api/types";

/**
 * Cookie names and JWT claim decoding, deliberately kept free of `server-only`
 * and `next/headers` so this can be imported from middleware, which runs in
 * the edge runtime and cannot use either. session.ts builds on this for the
 * Node-side cookie read/write.
 */
export const ACCESS_TOKEN_COOKIE = "dm_access";
export const REFRESH_TOKEN_COOKIE = "dm_refresh";

/** Each role's landing area, once signed in (Ref: SRS 3.13 roles). */
export const HOME_FOR_ROLE: Record<UserRole, string> = {
  STUDENT: "/learn",
  INSTRUCTOR: "/teach",
  ADMINISTRATOR: "/admin",
};

export interface SessionClaims {
  userId: string;
  email: string;
  role: UserRole;
  expiresAt: number;
}

/**
 * Reads the claims out of the access token WITHOUT verifying its signature.
 *
 * That is deliberate and safe: these claims only decide what to render and
 * where to redirect. Every actual authorization decision is made by the
 * backend, which does verify the signature (Ref: SRS 3.13 - RBAC is enforced
 * server-side). A forged cookie earns someone a menu item they cannot use and
 * a 403 the moment they try to use it.
 */
export function decodeClaims(token: string): SessionClaims | null {
  try {
    const payload = token.split(".")[1];
    if (!payload) return null;

    // atob rather than Buffer: middleware's edge runtime has no Buffer.
    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const json = atob(base64);
    const claims = JSON.parse(json) as { sub?: string; email?: string; role?: string; exp?: number };

    if (!claims.sub || !claims.role) return null;

    return {
      userId: claims.sub,
      email: claims.email ?? "",
      role: claims.role as UserRole,
      expiresAt: (claims.exp ?? 0) * 1000,
    };
  } catch {
    return null;
  }
}
