import "server-only";

import { fetchFromBackend } from "@/lib/api/server";
import type { SectionDetail } from "@/lib/api/types";

/** Ref: SRS 7.3, 7.9, 7.10 - non-admins/non-assigned-instructors only see published sections/lessons. */
export function getCourseOutline(courseId: string): Promise<SectionDetail[]> {
  return fetchFromBackend<SectionDetail[]>(`/api/v1/courses/${courseId}/sections`);
}
