"use client";

import { api } from "@/lib/api/client";
import type { CourseProgressSummary } from "@/lib/api/types";

/** Ref: SRS 12.4 - idempotent; reopening a completed lesson does not reset it. Manual, per-lesson completion only in v1. */
export function markLessonComplete(lessonId: string): Promise<CourseProgressSummary> {
  return api.post<CourseProgressSummary>(`/lessons/${lessonId}/progress/complete`);
}
