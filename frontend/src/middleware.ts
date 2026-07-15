import { NextRequest, NextResponse } from "next/server";
import { ACCESS_TOKEN_COOKIE, REFRESH_TOKEN_COOKIE, HOME_FOR_ROLE, decodeClaims } from "@/lib/auth/claims";

/**
 * Route guarding.
 *
 * This is a UX guard, not a security boundary: it decides what to *show* and
 * where to redirect, and it deliberately does not verify the JWT signature.
 * Every real authorization decision is made by the Spring backend, which does
 * verify it (Ref: SRS 3.13). Someone who forges a cookie gets a page shell and
 * a 403 from every call it makes.
 *
 * A missing access token is not treated as signed out on its own: the access
 * token lives 15 minutes while the refresh token lives 30 days, so a returning
 * user routinely has an expired access cookie and a perfectly good refresh
 * cookie. We let those through, and the BFF proxy silently refreshes on the
 * first call. Rejecting them here would sign people out every 15 minutes.
 */
const AREA_ROLE: Record<string, string> = {
  "/learn": "STUDENT",
  "/teach": "INSTRUCTOR",
  "/admin": "ADMINISTRATOR",
};

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  const accessToken = request.cookies.get(ACCESS_TOKEN_COOKIE)?.value;
  const hasRefreshToken = Boolean(request.cookies.get(REFRESH_TOKEN_COOKIE)?.value);
  const claims = accessToken ? decodeClaims(accessToken) : null;
  const signedIn = Boolean(claims) || hasRefreshToken;

  const protectedArea = Object.keys(AREA_ROLE).find((area) => pathname.startsWith(area));

  if (protectedArea && !signedIn) {
    const signIn = new URL("/sign-in", request.url);
    // Come back here once they've signed in, rather than dumping them on a
    // generic dashboard and making them navigate again.
    signIn.searchParams.set("next", pathname);
    return NextResponse.redirect(signIn);
  }

  // Wrong area for this role - send them to their own. Only possible when we
  // can actually read the role; otherwise the backend will 403 them anyway.
  if (protectedArea && claims && claims.role !== AREA_ROLE[protectedArea]) {
    return NextResponse.redirect(new URL(HOME_FOR_ROLE[claims.role] ?? "/", request.url));
  }

  // Already signed in? The sign-in/register pages have nothing to offer.
  if ((pathname === "/sign-in" || pathname === "/register") && claims) {
    return NextResponse.redirect(new URL(HOME_FOR_ROLE[claims.role] ?? "/", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/learn/:path*", "/teach/:path*", "/admin/:path*", "/sign-in", "/register"],
};
