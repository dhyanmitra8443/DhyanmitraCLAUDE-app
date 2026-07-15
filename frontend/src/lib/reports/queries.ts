import "server-only";

import { fetchFromBackend } from "@/lib/api/server";
import type { ReportData, ReportDefinition } from "@/lib/api/types";

/** Ref: SRS 15.3 - the reports available to the caller's role. */
export function listAvailableReports(): Promise<ReportDefinition[]> {
  return fetchFromBackend<ReportDefinition[]>("/api/v1/reports");
}

export interface ReportDataParams {
  page?: number;
  size?: number;
  dateFrom?: string;
  dateTo?: string;
  courseId?: string;
  search?: string;
}

/** Ref: SRS 15.4-15.11 - row shape depends on reportKey; results are always scoped to the caller's role/ownership. */
export function getReportData(reportKey: string, params: ReportDataParams = {}): Promise<ReportData> {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") query.set(key, String(value));
  }
  const qs = query.toString();
  return fetchFromBackend<ReportData>(`/api/v1/reports/${reportKey}${qs ? `?${qs}` : ""}`);
}
