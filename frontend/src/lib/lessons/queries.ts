import "server-only";

import { fetchFromBackend } from "@/lib/api/server";
import type { LessonDetail } from "@/lib/api/types";

/**
 * Ref: SRS 7.14 - throws (403) unless the caller is the admin/assigned
 * instructor, this is the published preview lesson, or the caller has an
 * active subscription to the course.
 */
export function getLessonDetail(lessonId: string): Promise<LessonDetail> {
  return fetchFromBackend<LessonDetail>(`/api/v1/lessons/${lessonId}`);
}
