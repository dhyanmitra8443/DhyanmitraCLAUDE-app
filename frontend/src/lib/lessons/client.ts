"use client";

import { api } from "@/lib/api/client";
import type { CreateLessonRequest, LessonDetail } from "@/lib/api/types";

/** Ref: SRS 7.7 - defaults to DRAFT. Administrator, or an instructor assigned to the course. */
export function createLesson(sectionId: string, payload: CreateLessonRequest): Promise<LessonDetail> {
  return api.post<LessonDetail>(`/sections/${sectionId}/lessons`, payload);
}

/** Ref: SRS 7.15 - reuses the create DTO (full replace); does not reset student progress. */
export function updateLesson(lessonId: string, payload: CreateLessonRequest): Promise<LessonDetail> {
  return api.patch<LessonDetail>(`/lessons/${lessonId}`, payload);
}

/** Ref: SRS 7.9 - requires the complete ordered set of lesson IDs belonging to the section. */
export function reorderLessons(sectionId: string, lessonIdsInOrder: string[]): Promise<null> {
  return api.patch<null>(`/sections/${sectionId}/lessons/reorder`, { lessonIdsInOrder });
}

/** Ref: SRS 7.6, 8.3, 8.7 - requires an active VIDEO resource attached (Ch.8), not just a videoUrl string. */
export function publishLesson(lessonId: string): Promise<null> {
  return api.post<null>(`/lessons/${lessonId}/publish`);
}

/** Ref: SRS 7.17 - lessons are never deleted, only archived. */
export function archiveLesson(lessonId: string): Promise<null> {
  return api.post<null>(`/lessons/${lessonId}/archive`);
}

/** Ref: SRS 7.11 - setting a new preview lesson automatically unsets the previous one. */
export function setLessonPreview(lessonId: string, isPreview: boolean): Promise<null> {
  return api.patch<null>(`/lessons/${lessonId}/preview`, { isPreview });
}
