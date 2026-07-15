"use client";

import { useRouter } from "next/navigation";
import { useTransition } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { archiveLesson, publishLesson } from "@/lib/lessons/client";

/** Ref: SRS 7.6, 8.3, 8.7 - publishing requires an active VIDEO resource (Ch.8), not just a videoUrl string. */
export function LessonStatusActions({
  lessonId,
  status,
}: {
  lessonId: string;
  status: "DRAFT" | "PUBLISHED" | "ARCHIVED";
}) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  function handlePublish() {
    startTransition(async () => {
      try {
        await publishLesson(lessonId);
        toast.success("Lesson published.");
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  function handleArchive() {
    startTransition(async () => {
      try {
        await archiveLesson(lessonId);
        toast.success("Lesson archived.");
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
