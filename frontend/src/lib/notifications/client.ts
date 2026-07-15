"use client";

import { api } from "@/lib/api/client";
import type { NotificationReadStatus, NotificationSummary, Paginated } from "@/lib/api/types";

/** Ref: SRS 14.4 - client-side fetch for the header notification bell (polls via TanStack Query). */
export function fetchOwnNotifications(
  params: { page?: number; size?: number; readStatus?: NotificationReadStatus } = {},
): Promise<Paginated<NotificationSummary>> {
  const query = new URLSearchParams();
  if (params.page != null) query.set("page", String(params.page));
  if (params.size != null) query.set("size", String(params.size));
  if (params.readStatus) query.set("readStatus", params.readStatus);
  const qs = query.toString();
  return api.get<Paginated<NotificationSummary>>(`/notifications/me${qs ? `?${qs}` : ""}`);
}

/** Ref: SRS 14.5 - users may only mark their own notifications. */
export function markNotificationRead(notificationId: string): Promise<null> {
  return api.patch<null>(`/notifications/${notificationId}/read`);
}

export function markAllNotificationsRead(): Promise<null> {
  return api.patch<null>("/notifications/read-all");
}
