"use client";

import { useRouter } from "next/navigation";
import { useTransition } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { archiveSection, publishSection } from "@/lib/sections/client";

/** Ref: SRS 7.4, 7.17 - publishing requires >=1 published lesson in the section. */
export function SectionStatusActions({
  sectionId,
  status,
}: {
  sectionId: string;
  status: "DRAFT" | "PUBLISHED" | "ARCHIVED";
}) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  function handlePublish() {
    startTransition(async () => {
      try {
        await publishSection(sectionId);
        toast.success("Section published.");
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  function handleArchive() {
    startTransition(async () => {
      try {
        await archiveSection(sectionId);
        toast.success("Section archived.");
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  if (status === "ARCHIVED") return null;

  return (
    <div className="flex gap-2">
      {status === "DRAFT" && (
        <Button size="sm" disabled={isPending} onClick={handlePublish}>
          {isPending ? "Publishing…" : "Publish"}
        </Button>
      )}
      <Button size="sm" variant="destructive" disabled={isPending} onClick={handleArchive}>
        {isPending ? "Archiving…" : "Archive"}
      </Button>
    </div>
  );
}
