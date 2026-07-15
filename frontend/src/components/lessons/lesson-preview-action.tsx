"use client";

import { useRouter } from "next/navigation";
import { useTransition } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { setLessonPreview } from "@/lib/lessons/client";

/** Ref: SRS 7.11 - exactly one preview lesson per course; setting a new one auto-unsets the previous one. */
export function LessonPreviewAction({ lessonId, isPreview }: { lessonId: string; isPreview: boolean }) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  function toggle() {
    startTransition(async () => {
      try {
        await setLessonPreview(lessonId, !isPreview);
        toast.success(isPreview ? "No longer the preview lesson." : "Set as the course preview lesson.");
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  return (
    <Button size="sm" variant={isPreview ? "secondary" : "outline"} disabled={isPending} onClick={toggle}>
      {isPending ? "Updating…" : isPreview ? "Unset preview lesson" : "Set as preview lesson"}
    </Button>
  );
}
