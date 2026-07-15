import "server-only";

import { cookies } from "next/headers";
import { ACCESS_TOKEN_COOKIE, REFRESH_TOKEN_COOKIE, decodeClaims, type SessionClaims } from "@/lib/auth/claims";

export { ACCESS_TOKEN_COOKIE, REFRESH_TOKEN_COOKIE, decodeClaims };
export type { SessionClaims };

/**
 * Ref: SRS 3.12, 17.6 - JWT session handling.
 *
 * Both tokens live in httpOnly cookies and are never exposed to client-side
 * JavaScript, so a single XSS bug cannot walk off with a user's session. The
 * browser therefore never calls the Spring backend directly: it calls this
 * Next app's BFF proxy (/api/backend/...), which attaches the access token
 * server-side. That also means no CORS, and no token in localStorage.
 */

/**
 * The refresh token outlives the access token (30 days vs 15 minutes, per the
 * backend's JWT_REFRESH_TTL_SECONDS / JWT_ACCESS_TTL_SECONDS), so its cookie
 * must too - otherwise sessions would die when the *cookie* expired rather
 * than when the token did.
 */
const REFRESH_MAX_AGE_SECONDS = 60 * 60 * 24 * 30;

const baseCookieOptions = {
  httpOnly: true,
  sameSite: "lax" as const,
  path: "/",
  // Secure in production only: a secure cookie is never sent over plain HTTP,
  // which would silently break local development on http://localhost.
  secure: process.env.NODE_ENV === "production",
};

export async function setSessionCookies(accessToken: string, refreshToken: string, accessTtlSeconds: number) {
  const store = await cookies();
  store.set(ACCESS_TOKEN_COOKIE, accessToken, { ...baseCookieOptions, maxAge: accessTtlSeconds });
  store.set(REFRESH_TOKEN_COOKIE, refreshToken, { ...baseCookieOptions, maxAge: REFRESH_MAX_AGE_SECONDS });
}

export async function clearSessionCookies() {
  const store = await cookies();
  store.delete(ACCESS_TOKEN_COOKIE);
  store.delete(REFRESH_TOKEN_COOKIE);
}

export async function getAccessToken(): Promise<string | undefined> {
  return (await cookies()).get(ACCESS_TOKEN_COOKIE)?.value;
}

export async function getRefreshToken(): Promise<string | undefined> {
  return (await cookies()).get(REFRESH_TOKEN_COOKIE)?.value;
}

/** The signed-in user's claims, or null when signed out. */
export async function getSession(): Promise<SessionClaims | null> {
  const token = await getAccessToken();
  if (!token) return null;
  return decodeClaims(token);
}
