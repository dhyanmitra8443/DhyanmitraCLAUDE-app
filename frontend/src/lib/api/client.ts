import type { ApiEnvelope } from "@/lib/api/types";
import { ApiError } from "@/lib/api/errors";

/**
 * Browser-side API client. Every call is same-origin to the BFF proxy
 * (/api/backend/...), which attaches the access token server-side - so no
 * token, and no backend URL, ever reaches this bundle.
 *
 * Unwraps the SRS 2.8 `{ success, message, data }` envelope so callers work
 * with the payload directly, and throws ApiError otherwise, which is what
 * TanStack Query surfaces as `error`.
 */
async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await fetch(`/api/backend${path}`, {
    ...init,
    headers: init.body instanceof FormData ? init.headers : { "Content-Type": "application/json", ...init.headers },
  });

  const text = await response.text();
  const body = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const envelope = body as { message?: string; errors?: { field: string; message: string }[] } | null;
    const fieldErrors: Record<string, string> = {};
    for (const item of envelope?.errors ?? []) {
      fieldErrors[item.field] = item.message;
    }
    throw new ApiError(response.status, envelope?.message ?? "Something went wrong. Please try again.", fieldErrors);
  }

  return (body as ApiEnvelope<T>).data;
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: "POST", body: body instanceof FormData ? body : JSON.stringify(body ?? {}) }),
  patch: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: "PATCH", body: JSON.stringify(body ?? {}) }),
  delete: <T>(path: string) => request<T>(path, { method: "DELETE" }),
};
