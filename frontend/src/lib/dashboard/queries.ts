import "server-only";

import { fetchFromBackend } from "@/lib/api/server";
import type { AdminDashboard, InstructorDashboard, StudentDashboard } from "@/lib/api/types";

/** Ref: SRS 13.4. */
export function getAdminDashboard(): Promise<AdminDashboard> {
  return fetchFromBackend<AdminDashboard>("/api/v1/dashboard/admin");
}

/** Ref: SRS 13.5. */
export function getInstructorDashboard(): Promise<InstructorDashboard> {
  return fetchFromBackend<InstructorDashboard>("/api/v1/dashboard/instructor");
}

/** Ref: SRS 13.6. */
export function getStudentDashboard(): Promise<StudentDashboard> {
  return fetchFromBackend<StudentDashboard>("/api/v1/dashboard/student");
}
