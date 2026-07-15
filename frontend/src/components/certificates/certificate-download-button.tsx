"use client";

import { useTransition } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { getCertificateDownloadUrl } from "@/lib/certificates/client";

/** Ref: SRS 12.12, 17.24 - fetches a short-lived signed download link, then opens it. */
export function CertificateDownloadButton({ certificateId }: { certificateId: string }) {
  const [isPending, startTransition] = useTransition();

  function handleDownload() {
    startTransition(async () => {
      try {
        const url = await getCertificateDownloadUrl(certificateId);
        window.open(url, "_blank");
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  return (
    <Button size="sm" variant="outline" disabled={isPending} onClick={handleDownload}>
      {isPending ? "Preparing…" : "Download"}
    </Button>
  );
}
