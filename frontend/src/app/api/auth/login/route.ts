import { NextRequest, NextResponse } from "next/server";
import { BACKEND_URL, toApiError } from "@/lib/api/server";
import { setSessionCookies } from "@/lib/auth/session";
import type { ApiEnvelope, AuthTokens } from "@/lib/api/types";

/**
 * Ref: SRS 3.6 - login. Kept out of the generic BFF proxy on purpose: this is
 * the one place tokens are turned into httpOnly cookies, and the response
 * deliberately returns only the user summary, never the tokens themselves.
 */
export async function POST(request: NextRequest) {
  const credentials = await request.text();

  const response = await fetch(`${BACKEND_URL}/api/v1/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: credentials,
    cache: "no-store",
  });

  const body = await response.json().catch(() => null);

  if (!response.ok) {
    const error = toApiError(response.status, body);
    return NextResponse.json(
      { success: false, message: error.message, errors: body?.errors ?? [] },
      { status: response.status },
    );
  }

  const tokens = (body as ApiEnvelope<AuthTokens>).data;
  await setSessionCookies(tokens.accessToken, tokens.refreshToken, tokens.expiresInSeconds);

  return NextResponse.json({ success: true, message: "Signed in.", data: tokens.user });
}
