"use client";

import { api } from "@/lib/api/client";
import { ApiError } from "@/lib/api/errors";
import type { CreateLessonResourceRequest, LessonResourceSummary } from "@/lib/api/types";

/** Ref: SRS 8.6 - same endpoint for every resource type, including VIDEO (resourceType + externalUrl). */
export function createLessonResource(
  lessonId: string,
  payload: CreateLessonResourceRequest,
): Promise<LessonResourceSummary> {
  return api.post<LessonResourceSummary>(`/lessons/${lessonId}/resources`, payload);
}

/** Ref: SRS 8.13 - reuses the create DTO (full replace). */
export function updateLessonResource(
  resourceId: string,
  payload: CreateLessonResourceRequest,
): Promise<LessonResourceSummary> {
  return api.patch<LessonResourceSummary>(`/resources/${resourceId}`, payload);
}

/**
 * Ref: SRS 8.14 - resources are never deleted, only archived. Archiving the
 * lesson's only active VIDEO resource while the lesson is PUBLISHED is
 * rejected (409) by the backend, not auto-handled - the UI must archive a
 * replacement video in first if swapping.
 */
export function archiveLessonResource(resourceId: string): Promise<null> {
  return api.post<null>(`/resources/${resourceId}/archive`);
}

interface UploadUrlResponse {
  uploadUrl: string;
  fileReference: string;
  expiresInSeconds: number;
}

/**
 * Ref: SRS 8.8, 8.15 - two-step upload. The backend returns an absolute
 * `/api/v1/...` path (not proxied), so it's rewritten to go through the same
 * `/api/backend` BFF proxy every other call uses, rather than hitting the
 * backend directly from the browser.
 */
async function getUploadUrl(fileName: string, contentType: string): Promise<UploadUrlResponse> {
  return api.post<UploadUrlResponse>("/lesson-resources/upload-url", { fileName, contentType });
}

const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;

/**
 * Uploads a file (PDF/IMAGE/AUDIO/ZIP) and returns the `fileReference` to
 * pass as `CreateLessonResourceRequest.fileReference`. Ref: SRS 8.8 - the
 * backend enforces only a global 5MB cap (shared with profile photos, no
 * per-type limit or content-type allowlist), so that's all this checks too.
 */
export async function uploadResourceFile(file: File): Promise<string> {
  if (file.size > MAX_FILE_SIZE_BYTES) {
    throw new ApiError(400, "File must be 5 MB or smaller.");
  }

  const { uploadUrl, fileReference } = await getUploadUrl(file.name, file.type || "application/octet-stream");
  const proxyPath = uploadUrl.replace(/^\/api\/v1/, "/api/backend");

  const formData = new FormData();
  formData.append("file", file);
  const response = await fetch(proxyPath, { method: "POST", body: formData });

  if (!response.ok) {
    throw new ApiError(response.status, "File upload failed. Please try again.");
  }

  return fileReference;
}

/**
 * Ref: SRS 8.12 - short-lived download link; VIDEO/EXTERNAL_LINK resources
 * use their externalUrl directly instead (the backend rejects this call for
 * those types).
 */
export async function getResourceDownloadUrl(resourceId: string): Promise<string> {
  const { downloadUrl } = await api.get<{ downloadUrl: string; expiresInSeconds: number }>(
    `/resources/${resourceId}/download`,
  );
  return downloadUrl.replace(/^\/api\/v1/, "/api/backend");
}
