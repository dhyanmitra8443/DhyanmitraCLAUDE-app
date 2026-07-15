import "server-only";

import { fetchFromBackend } from "@/lib/api/server";
import type { CourseProgressSummary, Paginated } from "@/lib/api/types";

/** Ref: SRS 12.6 - the authenticated student's own progress in a course. */
export function getOwnCourseProgress(courseId: string): Promise<CourseProgressSummary> {
  return fetchFromBackend<CourseProgressSummary>(`/api/v1/courses/${courseId}/progress/me`);
}

/** Ref: SRS 12.7 - administrator, or the assigned instructor for this course. No per-student name resolution (studentId only). */
export function listCourseProgress(
  courseId: string,
  params: { page?: number; size?: number } = {},
): Promise<Paginated<CourseProgressSummary>> {
  const query = new URLSearchParams();
  if (params.page != null) query.set("page", String(params.page));
  if (params.size != null) query.set("size", String(params.size));
  const qs = query.toString();
  return fetchFromBackend<Paginated<CourseProgressSummary>>(`/api/v1/courses/${courseId}/progress${qs ? `?${qs}` : ""}`);
}
