import { NextRequest, NextResponse } from "next/server";
import { BACKEND_URL } from "@/lib/api/server";

/**
 * Ref: SRS 3.3 - student self-registration. Public (no token), and it does not
 * sign the user in: the backend returns the created profile, not a session, so
 * the UI sends them to the sign-in page afterwards.
 */
export async function POST(request: NextRequest) {
  const payload = await request.text();

  const response = await fetch(`${BACKEND_URL}/api/v1/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: payload,
    cache: "no-store",
  });

  const body = await response.json().catch(() => null);
  return NextResponse.json(body, { status: response.status });
}
