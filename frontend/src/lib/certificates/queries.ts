import "server-only";

import { fetchFromBackend } from "@/lib/api/server";
import type { CertificateSummary, CertificateVerifyResponse, Paginated } from "@/lib/api/types";

/** Ref: SRS 12.11 - the authenticated student's own earned certificates. */
export function getMyCertificates(): Promise<CertificateSummary[]> {
  return fetchFromBackend<CertificateSummary[]>("/api/v1/certificates/me");
}

export interface SearchCertificatesParams {
  page?: number;
  size?: number;
  sort?: string;
  studentName?: string;
  courseId?: string;
  certificateNumber?: string;
}

/** Ref: SRS 12.14 - administrator-only. */
export function searchCertificates(params: SearchCertificatesParams): Promise<Paginated<CertificateSummary>> {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") query.set(key, String(value));
  }
  const qs = query.toString();
  return fetchFromBackend<Paginated<CertificateSummary>>(`/api/v1/certificates${qs ? `?${qs}` : ""}`);
}

/** Ref: SRS 12.11 - administrator, assigned instructor, or the owning student. */
export function getCertificateDetail(certificateId: string): Promise<CertificateSummary> {
  return fetchFromBackend<CertificateSummary>(`/api/v1/certificates/${certificateId}`);
}

/** Ref: SRS 12.13, 12.17 - public, unauthenticated; unknown IDs 404 with no data leakage. */
export function verifyCertificate(verificationId: string): Promise<CertificateVerifyResponse> {
  return fetchFromBackend<CertificateVerifyResponse>(`/api/v1/certificates/verify/${verificationId}`);
}
