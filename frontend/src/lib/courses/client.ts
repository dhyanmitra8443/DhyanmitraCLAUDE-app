"use client";

import { api } from "@/lib/api/client";
import type { CourseDetail, CreateCourseRequest } from "@/lib/api/types";

/** Ref: SRS 5.5 - defaults to DRAFT. Allowed for administrators and instructors. */
export function createCourse(payload: CreateCourseRequest): Promise<CourseDetail> {
  return api.post<CourseDetail>("/courses", payload);
}

/** Ref: SRS 5.11 - administrator, or an instructor assigned to this course. */
export function updateCourse(courseId: string, payload: CreateCourseRequest): Promise<CourseDetail> {
  return api.patch<CourseDetail>(`/courses/${courseId}`, payload);
}

/** Ref: SRS 5.4 - requires >=1 category, >=1 instructor, >=1 published lesson, >=1 active plan. */
export function publishCourse(courseId: string): Promise<null> {
  return api.post<null>(`/courses/${courseId}/publish`);
}

/** Ref: SRS 5.12 - courses are never deleted, only archived. */
export function archiveCourse(courseId: string): Promise<null> {
  return api.post<null>(`/courses/${courseId}/archive`);
}

/** Ref: SRS 5.6, 4.8 - administrator-only. */
export function assignInstructor(courseId: string, instructorId: string): Promise<null> {
  return api.post<null>(`/courses/${courseId}/instructors`, { instructorId });
}

/** Ref: SRS 5.6 - administrator-only; cannot remove the last instructor from a published course. */
export function removeInstructor(courseId: string, instructorId: string): Promise<null> {
  return api.delete<null>(`/courses/${courseId}/instructors/${instructorId}`);
}

/** Ref: SRS 5.7, 6.6 - administrator, or an instructor assigned to this course. */
export function assignCategories(courseId: string, categoryIds: string[]): Promise<null> {
  return api.post<null>(`/courses/${courseId}/categories`, { categoryIds });
}

/** Ref: SRS 5.7 - cannot remove the last category from a published course. */
export function removeCategory(courseId: string, categoryId: string): Promise<null> {
  return api.delete<null>(`/courses/${courseId}/categories/${categoryId}`);
}
