import "server-only";

import { getAccessToken, getRefreshToken, setSessionCookies } from "@/lib/auth/session";
import type { ApiEnvelope, AuthTokens } from "@/lib/api/types";
import { ApiError } from "@/lib/api/errors";

/**
 * The Spring backend's base URL. Server-side only - the browser never talks to
 * it directly (see session.ts), so this is not a NEXT_PUBLIC_ variable and
 * cannot leak into the client bundle.
 */
export const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

interface BackendResponse {
  status: number;
  body: unknown;
}

/**
 * One raw call to the backend. Returns the parsed body rather than throwing,
 * because the BFF proxy needs to inspect a 401 before deciding whether to
 * refresh and retry.
 */
async function call(path: string, init: RequestInit, accessToken?: string): Promise<BackendResponse> {
  const headers = new Headers(init.headers);
  if (accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  const response = await fetch(`${BACKEND_URL}${path}`, {
    ...init,
    headers,
    // Reports and dashboards must never be served from a stale cache; every
    // one of these calls is user-specific and authenticated.
    cache: "no-store",
  });

  const text = await response.text();
  let body: unknown = null;
  if (text) {
    try {
      body = JSON.parse(text);
    } catch {
      body = text; // non-JSON (a file download, or an upstream error page)
    }
  }

  return { status: response.status, body };
}

/**
 * Exchanges the refresh token for a fresh access token and re-issues the
 * cookies. Returns the new access token, or null when the refresh token is
 * itself expired/revoked - in which case the caller must treat the user as
 * signed out.
 */
async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = await getRefreshToken();
  if (!refreshToken) return null;

  const { status, body } = await call("/api/v1/auth/refresh-token", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });

  if (status !== 200) return null;

  const tokens = (body as ApiEnvelope<AuthTokens>).data;
  await setSessionCookies(tokens.accessToken, tokens.refreshToken, tokens.expiresInSeconds);
  return tokens.accessToken;
}

/**
 * Calls the backend as the signed-in user, transparently refreshing the access
 * token once if it has expired.
 *
 * The retry is attempted exactly once: if the refreshed token is *also*
 * rejected, that is a real authorization failure, and retrying again would
 * just loop.
 */
export async function callBackendAsUser(path: string, init: RequestInit): Promise<BackendResponse> {
  const accessToken = await getAccessToken();
  const first = await call(path, init, accessToken);

  if (first.status !== 401) {
    return first;
  }

  const refreshed = await refreshAccessToken();
  if (!refreshed) {
    return first;
  }

  return call(path, init, refreshed);
}

/**
 * Server-component data fetching: calls the backend, unwraps the SRS 2.8
 * envelope, and throws ApiError on failure so an error boundary can render it.
 */
export async function fetchFromBackend<T>(path: string, init: RequestInit = {}): Promise<T> {
  const { status, body } = await callBackendAsUser(path, init);

  if (status >= 400) {
    throw toApiError(status, body);
  }

  return (body as ApiEnvelope<T>).data;
}

export function toApiError(status: number, body: unknown): ApiError {
  const envelope = body as { message?: string; errors?: { field: string; message: string }[] } | null;

  const fieldErrors: Record<string, string> = {};
  for (const item of envelope?.errors ?? []) {
    fieldErrors[item.field] = item.message;
  }

  return new ApiError(status, envelope?.message ?? "Something went wrong. Please try again.", fieldErrors);
}
