"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useTransition } from "react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { ReorderButtons } from "@/components/shared/reorder-buttons";
import { ApiError } from "@/lib/api/errors";
import { reorderLessons } from "@/lib/lessons/client";
import type { LessonSummary } from "@/lib/api/types";

const STATUS_VARIANT = {
  DRAFT: "secondary",
  PUBLISHED: "default",
  ARCHIVED: "outline",
} as const;

/** Ref: SRS 7.5, 7.9 - the section's lesson list, with manual reordering. */
export function LessonListManager({
  sectionId,
  lessons,
  basePath,
}: {
  sectionId: string;
  lessons: LessonSummary[];
  basePath: string;
}) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  function move(index: number, direction: -1 | 1) {
    const next = [...lessons];
    const target = index + direction;
    if (target < 0 || target >= next.length) return;
    [next[index], next[target]] = [next[target], next[index]];

    startTransition(async () => {
      try {
        await reorderLessons(
          sectionId,
          next.map((l) => l.id).filter((id): id is string => Boolean(id)),
        );
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  return (
    <div className="space-y-3">
      {lessons.length === 0 ? (
        <p className="text-muted-foreground text-sm">No lessons yet.</p>
      ) : (
        <ul className="space-y-2">
          {lessons.map((lesson, index) => (
            <li key={lesson.id} className="flex items-center gap-2 rounded-lg border px-3 py-2">
              <ReorderButtons
                disabled={isPending}
                disableUp={index === 0}
                disableDown={index === lessons.length - 1}
                onMoveUp={() => move(index, -1)}
                onMoveDown={() => move(index, 1)}
              />
              <Link href={`${basePath}/lessons/${lesson.id}`} className="flex-1 hover:underline">
                {lesson.title}
              </Link>
              {lesson.isPreview && <Badge variant="outline">Preview</Badge>}
              {lesson.status && <Badge variant={STATUS_VARIANT[lesson.status]}>{lesson.status}</Badge>}
            </li>
          ))}
        </ul>
      )}

      <Link href={`${basePath}/lessons/new`} className={cn(buttonVariants({ variant: "outline", size: "sm" }))}>
        Add lesson
      </Link>
    </div>
  );
}
