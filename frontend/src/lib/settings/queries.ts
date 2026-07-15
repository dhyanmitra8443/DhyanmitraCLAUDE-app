import "server-only";

import { fetchFromBackend } from "@/lib/api/server";
import type { SystemSettings } from "@/lib/api/types";

/** Ref: SRS Chapter 16 - System Settings. Administrator-only. */
export function getSystemSettings(): Promise<SystemSettings> {
  return fetchFromBackend<SystemSettings>("/api/v1/settings");
}
