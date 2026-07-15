"use client";

import { api } from "@/lib/api/client";
import type { CertificateDownloadUrlResponse } from "@/lib/api/types";

/**
 * Ref: SRS 12.12, 17.24 - pre-signed, time-limited download link for the
 * certificate PDF. The backend returns an absolute `/api/v1/...` path (not
 * proxied), so it's rewritten to go through the same `/api/backend` BFF
 * proxy every other call uses, rather than hitting the backend directly
 * from the browser (Ref: lib/resources/client.ts's identical pattern).
 */
export async function getCertificateDownloadUrl(certificateId: string): Promise<string> {
  const { downloadUrl } = await api.get<CertificateDownloadUrlResponse>(`/certificates/${certificateId}/download`);
  return downloadUrl.replace(/^\/api\/v1/, "/api/backend");
}
