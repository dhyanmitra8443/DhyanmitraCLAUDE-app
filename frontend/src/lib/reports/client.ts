"use client";

import { ApiError } from "@/lib/api/errors";
import type { ExportStatusResponse, ReportFormat } from "@/lib/api/types";

export interface ReportFilterParams {
  dateFrom?: string;
  dateTo?: string;
  courseId?: string;
  search?: string;
}

function buildExportQuery(format: ReportFormat, params: ReportFilterParams): string {
  const query = new URLSearchParams({ format });
  if (params.dateFrom) query.set("dateFrom", params.dateFrom);
  if (params.dateTo) query.set("dateTo", params.dateTo);
  if (params.courseId) query.set("courseId", params.courseId);
  if (params.search) query.set("search", params.search);
  return query.toString();
}

function triggerBrowserDownload(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

function filenameFor(reportKey: string, format: ReportFormat): string {
  return `${reportKey.toLowerCase()}.${format.toLowerCase()}`;
}

/** Ref: SRS 15.15 - polls until the async export job leaves PROCESSING, or gives up after ~60s. */
async function pollExportStatus(exportJobId: string, attempts = 30, intervalMs = 2000): Promise<ExportStatusResponse> {
  for (let i = 0; i < attempts; i++) {
    const response = await fetch(`/api/backend/reports/exports/${exportJobId}`);
    const body = await response.json();
    if (!response.ok) throw new ApiError(response.status, body?.message ?? "Export failed.");
    const status = body.data as ExportStatusResponse;
    if (status.status !== "PROCESSING") return status;
    await new Promise((resolve) => setTimeout(resolve, intervalMs));
  }
  throw new ApiError(504, "Export is taking longer than expected. Please try again shortly.");
}

/**
 * Ref: SRS 15.9, 15.15 - handles both the synchronous (small dataset, file
 * streamed directly with a 200) and asynchronous (large dataset, 202 +
 * pollable job) export paths transparently, since the caller can't know in
 * advance which one the backend will pick for a given report/filter
 * combination. Goes through fetch() directly rather than lib/api/client.ts's
 * `api` helper, since that helper always JSON-parses the response body -
 * wrong for the synchronous case, which returns raw file bytes.
 */
export async function exportReport(reportKey: string, format: ReportFormat, params: ReportFilterParams): Promise<void> {
  const qs = buildExportQuery(format, params);
  const response = await fetch(`/api/backend/reports/${reportKey}/export?${qs}`);

  if (response.status === 202) {
    const body = await response.json();
    const status = await pollExportStatus(body.data.exportJobId);
    if (status.status !== "READY" || !status.downloadUrl) {
      throw new ApiError(500, "Export failed. Please try again.");
    }
    window.open(status.downloadUrl.replace(/^\/api\/v1/, "/api/backend"), "_blank");
    return;
  }

  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new ApiError(response.status, body?.message ?? "Export failed. Please try again.");
  }

  const blob = await response.blob();
  triggerBrowserDownload(blob, filenameFor(reportKey, format));
}
