"use client";

import { api } from "@/lib/api/client";
import type { CreateSectionRequest, SectionDetail } from "@/lib/api/types";

/** Ref: SRS 7.4 - defaults to DRAFT. Administrator, or an instructor assigned to the course. */
export function createSection(courseId: string, payload: CreateSectionRequest): Promise<SectionDetail> {
  return api.post<SectionDetail>(`/courses/${courseId}/sections`, payload);
}

/** Ref: SRS 7.16 - reuses the create DTO (full replace). */
export function updateSection(sectionId: string, payload: CreateSectionRequest): Promise<SectionDetail> {
  return api.patch<SectionDetail>(`/sections/${sectionId}`, payload);
}

/** Ref: SRS 7.10 - requires the complete ordered set of section IDs belonging to the course. */
export function reorderSections(courseId: string, sectionIdsInOrder: string[]): Promise<null> {
  return api.patch<null>(`/courses/${courseId}/sections/reorder`, { sectionIdsInOrder });
}

/** Ref: SRS 7.4 - requires at least one published lesson in the section. */
export function publishSection(sectionId: string): Promise<null> {
  return api.post<null>(`/sections/${sectionId}/publish`);
}

/** Ref: SRS 7.17 - sections are never deleted, only archived. */
export function archiveSection(sectionId: string): Promise<null> {
  return api.post<null>(`/sections/${sectionId}/archive`);
}
