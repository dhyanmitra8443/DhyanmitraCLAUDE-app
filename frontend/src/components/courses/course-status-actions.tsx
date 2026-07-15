"use client";

import { useRouter } from "next/navigation";
import { useTransition } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { archiveCourse, publishCourse } from "@/lib/courses/client";

/** Ref: SRS 5.4, 5.12 - DRAFT -> PUBLISHED -> ARCHIVED; archiving is the only "deletion" a course ever gets. */
export function CourseStatusActions({
  courseId,
  status,
}: {
  courseId: string;
  status: "DRAFT" | "PUBLISHED" | "ARCHIVED";
}) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  function handlePublish() {
    startTransition(async () => {
      try {
        await publishCourse(courseId);
        toast.success("Course published.");
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  function handleArchive() {
    startTransition(async () => {
      try {
        await archiveCourse(courseId);
        toast.success("Course archived.");
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
