"use client";

import { useTransition } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { getResourceDownloadUrl } from "@/lib/resources/client";

/** Ref: SRS 8.12 - fetches a short-lived signed download link, then opens it (server sets Content-Disposition: attachment). */
export function ResourceDownloadButton({ resourceId, label = "Download" }: { resourceId: string; label?: string }) {
  const [isPending, startTransition] = useTransition();

  function handleDownload() {
    startTransition(async () => {
      try {
        const url = await getResourceDownloadUrl(resourceId);
        window.open(url, "_blank");
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  return (
    <Button size="sm" variant="outline" disabled={isPending} onClick={handleDownload}>
      {isPending ? "Preparing…" : label}
    </Button>
  );
}
