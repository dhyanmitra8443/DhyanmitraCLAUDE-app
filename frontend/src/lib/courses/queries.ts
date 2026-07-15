import "server-only";

import { fetchFromBackend } from "@/lib/api/server";
import type { CourseDetail, CourseSummary, Paginated } from "@/lib/api/types";

export interface ListCoursesParams {
  page?: number;
  size?: number;
  sort?: string;
  search?: string;
  categoryId?: string[];
  instructorId?: string;
  difficultyLevel?: "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
  language?: string;
  /** Administrator-only filter (Ref: SRS 5.13). */
  status?: "DRAFT" | "PUBLISHED" | "ARCHIVED";
}

/** Ref: SRS 5.13, 5.14 - non-admins are always forced server-side to PUBLISHED (SRS 5.9). */
export function listCourses(params: ListCoursesParams): Promise<Paginated<CourseSummary>> {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === "") continue;
    if (Array.isArray(value)) {
      for (const item of value) query.append(key, item);
    } else {
      query.set(key, String(value));
    }
  }
  const qs = query.toString();
  return fetchFromBackend<Paginated<CourseSummary>>(`/api/v1/courses${qs ? `?${qs}` : ""}`);
}

/** Ref: SRS 5.15. */
export function getCourseDetail(courseId: string): Promise<CourseDetail> {
  return fetchFromBackend<CourseDetail>(`/api/v1/courses/${courseId}`);
}
