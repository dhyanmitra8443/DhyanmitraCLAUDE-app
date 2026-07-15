import { NextRequest, NextResponse } from "next/server";
import { BACKEND_URL } from "@/lib/api/server";

/**
 * Unauthenticated passthrough for the backend's public static uploads
 * (profile photos - see LocalFileStorageService's public/ subtree). These
 * aren't under /api/v1, so the main BFF proxy (api/backend/[...path]) can't
 * reach them, and the raw /uploads/... path the backend returns resolves
 * against the frontend's own origin if rendered as-is in an <img src>.
 */
export async function GET(request: NextRequest, { params }: { params: Promise<{ path: string[] }> }) {
  const { path } = await params;
  const response = await fetch(`${BACKEND_URL}/uploads/${path.join("/")}`, { cache: "no-store" });

  if (!response.ok) {
    return new NextResponse(null, { status: response.status });
  }

  const buffer = await response.arrayBuffer();
  const headers = new Headers();
  headers.set("Content-Type", response.headers.get("content-type") ?? "application/octet-stream");
  return new NextResponse(buffer, { status: 200, headers });
}
