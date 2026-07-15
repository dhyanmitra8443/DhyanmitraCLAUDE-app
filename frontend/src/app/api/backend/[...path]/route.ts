import { NextRequest, NextResponse } from "next/server";
import { callBackendAsUser } from "@/lib/api/server";

/**
 * BFF proxy: the single door between the browser and the Spring backend.
 *
 * Client components fetch same-origin `/api/backend/<path>` and this handler
 * forwards to the backend with the access token taken from the httpOnly
 * cookie - so the token never enters the client bundle, and an expired one is
 * refreshed transparently mid-request (see callBackendAsUser). It also means
 * the backend needs no CORS allowance for the browser at all.
 */
async function proxy(request: NextRequest, path: string[]) {
  const search = request.nextUrl.search;
  const target = `/api/v1/${path.join("/")}${search}`;

  const method = request.method;
  const hasBody = method !== "GET" && method !== "HEAD" && method !== "DELETE";

  const init: RequestInit = { method };

  if (hasBody) {
    const contentType = request.headers.get("content-type") ?? "";
    if (contentType.includes("multipart/form-data")) {
      // Pass file uploads (profile photo, lesson resources) straight through.
      // The boundary lives in the original Content-Type header, so fetch must
      // set it from the FormData itself - do not copy the header manually.
      init.body = await request.formData();
    } else {
      init.body = await request.text();
      init.headers = { "Content-Type": "application/json" };
    }
  }

  const { status, body } = await callBackendAsUser(target, init);

  // 204 and friends must not carry a body.
  if (status === 204 || body === null) {
    return new NextResponse(null, { status });
  }

  return NextResponse.json(body, { status });
}

export async function GET(request: NextRequest, { params }: { params: Promise<{ path: string[] }> }) {
  return proxy(request, (await params).path);
}

export async function POST(request: NextRequest, { params }: { params: Promise<{ path: string[] }> }) {
  return proxy(request, (await params).path);
}

export async function PATCH(request: NextRequest, { params }: { params: Promise<{ path: string[] }> }) {
  return proxy(request, (await params).path);
}

export async function PUT(request: NextRequest, { params }: { params: Promise<{ path: string[] }> }) {
  return proxy(request, (await params).path);
}

export async function DELETE(request: NextRequest, { params }: { params: Promise<{ path: string[] }> }) {
  return proxy(request, (await params).path);
}
