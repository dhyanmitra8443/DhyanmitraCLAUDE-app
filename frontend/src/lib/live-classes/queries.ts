import "server-only";

import { fetchFromBackend } from "@/lib/api/server";
import type { LiveClassStatus, LiveClassSummary, Paginated } from "@/lib/api/types";

/** Ref: SRS 11.8 - students see this only with an active subscription; admin/assigned instructor always. */
export function listLiveClassesByCourse(
  courseId: string,
  params: { page?: number; size?: number } = {},
): Promise<Paginated<LiveClassSummary>> {
  const query = new URLSearchParams();
  if (params.page != null) query.set("page", String(params.page));
  if (params.size != null) query.set("size", String(params.size));
  const qs = query.toString();
  return fetchFromBackend<Paginated<LiveClassSummary>>(`/api/v1/courses/${courseId}/live-classes${qs ? `?${qs}` : ""}`);
}

export interface SearchLiveClassesParams {
  page?: number;
  size?: number;
  sort?: string;
  courseId?: string;
  status?: LiveClassStatus;
  date?: string;
}

/** Ref: SRS 11.14 - administrator/instructor cross-course view. */
export function searchLiveClasses(params: SearchLiveClassesParams): Promise<Paginated<LiveClassSummary>> {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") query.set(key, String(value));
  }
  const qs = query.toString();
  return fetchFromBackend<Paginated<LiveClassSummary>>(`/api/v1/live-classes${qs ? `?${qs}` : ""}`);
}

/** Ref: SRS 11.6 - meetingUrl is nulled out server-side unless the caller is authorized to see it. */
export function getLiveClassDetail(liveClassId: string): Promise<LiveClassSummary> {
  return fetchFromBackend<LiveClassSummary>(`/api/v1/live-classes/${liveClassId}`);
}
