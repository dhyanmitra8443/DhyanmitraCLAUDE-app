import "server-only";

import { fetchFromBackend } from "@/lib/api/server";
import type { NotificationDeliveryChannel, NotificationReadStatus, NotificationSummary, Paginated } from "@/lib/api/types";

/** Ref: SRS 14.4 - the authenticated user's own notifications, any role. */
export function getOwnNotifications(
  params: { page?: number; size?: number; readStatus?: NotificationReadStatus } = {},
): Promise<Paginated<NotificationSummary>> {
  const query = new URLSearchParams();
  if (params.page != null) query.set("page", String(params.page));
  if (params.size != null) query.set("size", String(params.size));
  if (params.readStatus) query.set("readStatus", params.readStatus);
  const qs = query.toString();
  return fetchFromBackend<Paginated<NotificationSummary>>(`/api/v1/notifications/me${qs ? `?${qs}` : ""}`);
}

export interface SearchNotificationsParams {
  page?: number;
  size?: number;
  userId?: string;
  notificationType?: string;
  deliveryChannel?: NotificationDeliveryChannel;
}

/** Ref: SRS 14.13 - administrator-only system-wide log. `notificationType` actually filters by relatedModule server-side (Ref: NotificationService.searchNotifications), but the wire param name is `notificationType` per openapi.yaml - keeping the frontend name in sync with the contract, not the backend's internal naming. */
export function searchNotifications(params: SearchNotificationsParams): Promise<Paginated<NotificationSummary>> {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") query.set(key, String(value));
  }
  const qs = query.toString();
  return fetchFromBackend<Paginated<NotificationSummary>>(`/api/v1/notifications${qs ? `?${qs}` : ""}`);
}
