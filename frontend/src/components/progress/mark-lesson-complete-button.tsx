"use client";

import { useRouter } from "next/navigation";
import { useTransition } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { markLessonComplete } from "@/lib/progress/client";

/** Ref: SRS 12.4 - idempotent; safe to click again on an already-completed lesson. */
export function MarkLessonCompleteButton({ lessonId }: { lessonId: string }) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  function onClick() {
    startTransition(async () => {
      try {
        const progress = await markLessonComplete(lessonId);
        toast.success(`Lesson complete! ${Math.round(progress.progressPercentage ?? 0)}% of the course done.`);
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  return (
    <Button size="sm" disabled={isPending} onClick={onClick}>
      {isPending ? "Saving…" : "Mark lesson complete"}
    </Button>
  );
}
