import "server-only";

import { fetchFromBackend } from "@/lib/api/server";
import type { LessonResourceSummary } from "@/lib/api/types";

/**
 * Ref: SRS 8.4, 8.11 - same access rule as the lesson itself (Ch.7); students
 * see only ACTIVE resources. Note: LessonDetail.resources is a permanently
 * empty placeholder on the backend - this is the only real source for a
 * lesson's resources, always fetch it separately.
 */
export function listLessonResources(lessonId: string): Promise<LessonResourceSummary[]> {
  return fetchFromBackend<LessonResourceSummary[]>(`/api/v1/lessons/${lessonId}/resources`);
}
