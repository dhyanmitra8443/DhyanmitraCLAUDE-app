"use client";

import { api } from "@/lib/api/client";
import type { CreateLiveClassRequest, JoinLiveClassResponse, LiveClassSummary } from "@/lib/api/types";

/** Ref: SRS 11.6, 11.7 - administrator or assigned instructor. Recurring schedules are not supported in v1. */
export function createLiveClass(courseId: string, payload: CreateLiveClassRequest): Promise<LiveClassSummary> {
  return api.post<LiveClassSummary>(`/courses/${courseId}/live-classes`, payload);
}

/** Ref: SRS 11.12 - reuses the create DTO (full replace); does not affect attendance history. */
export function updateLiveClass(liveClassId: string, payload: CreateLiveClassRequest): Promise<null> {
  return api.patch<null>(`/live-classes/${liveClassId}`, payload);
}

/** Ref: SRS 11.13 - administrator or assigned instructor. */
export function cancelLiveClass(liveClassId: string): Promise<null> {
  return api.post<null>(`/live-classes/${liveClassId}/cancel`);
}

/** Ref: SRS 11.8-11.10 - student-only; records attendance and returns the meeting URL. */
export function joinLiveClass(liveClassId: string): Promise<JoinLiveClassResponse> {
  return api.post<JoinLiveClassResponse>(`/live-classes/${liveClassId}/join`);
}

/** Ref: SRS 11.11 - optional, added after the session completes. */
export function addRecordingUrl(liveClassId: string, recordingUrl: string): Promise<null> {
  return api.post<null>(`/live-classes/${liveClassId}/recording`, { recordingUrl });
}
