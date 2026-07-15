import { NextRequest, NextResponse } from "next/server";
import { callBackendRawAsUser } from "@/lib/api/server";

/**
 * BFF proxy: the single door between the browser and the Spring backend.
 *
 * Client components fetch same-origin `/api/backend/<path>` and this handler
 * forwards to the backend with the access token taken from the httpOnly
 * cookie - so the token never enters the client bundle, and an expired one is
 * refreshed transparently mid-request (see callBackendRawAsUser). It also
 * means the backend needs no CORS allowance for the browser at all.
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

  const response = await callBackendRawAsUser(target, init);

  if (response.status === 204) {
    return new NextResponse(null, { status: response.status });
  }

  const responseContentType = response.headers.get("content-type") ?? "";
  if (responseContentType.includes("application/json")) {
    const text = await response.text();
    const body = text ? JSON.parse(text) : null;
    if (body === null) {
      return new NextResponse(null, { status: response.status });
    }
    return NextResponse.json(body, { status: response.status });
  }

  // Binary passthrough (report exports, certificate/lesson-resource file
  // downloads): read raw bytes and preserve the upstream Content-Type and
  // Content-Disposition (the filename the browser should save as) instead
  // of decoding as text and re-wrapping as JSON, which would corrupt
  // anything that isn't valid UTF-8.
  const buffer = await response.arrayBuffer();
  const headers = new Headers();
  headers.set("Content-Type", responseContentType || "application/octet-stream");
  const contentDisposition = response.headers.get("content-disposition");
  if (contentDisposition) headers.set("Content-Disposition", contentDisposition);
  return new NextResponse(buffer, { status: response.status, headers });
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
