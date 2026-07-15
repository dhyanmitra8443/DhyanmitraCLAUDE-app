import { NextResponse } from "next/server";
import { callBackendAsUser } from "@/lib/api/server";
import { clearSessionCookies } from "@/lib/auth/session";

/**
 * Ref: SRS 3.7 - logout ends the server-side session (the backend enforces a
 * single active session per user), so we tell the backend first and clear the
 * cookies regardless of how that call goes: a user who clicked "sign out" must
 * end up signed out of this browser even if the backend is unreachable.
 */
export async function POST() {
  try {
    await callBackendAsUser("/api/v1/auth/logout", { method: "POST" });
  } catch {
    // Deliberately ignored - see above.
  }

  await clearSessionCookies();
  return NextResponse.json({ success: true, message: "Signed out.", data: null });
}
