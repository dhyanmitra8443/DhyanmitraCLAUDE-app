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
import { reorderSections } from "@/lib/sections/client";
import type { SectionDetail } from "@/lib/api/types";

const STATUS_VARIANT = {
  DRAFT: "secondary",
  PUBLISHED: "default",
  ARCHIVED: "outline",
} as const;

/** Ref: SRS 7.3, 7.9, 7.10 - the course's section outline, with manual reordering. */
export function CourseContentManager({
  courseId,
  sections,
  basePath,
}: {
  courseId: string;
  sections: SectionDetail[];
  basePath: string;
}) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  function move(index: number, direction: -1 | 1) {
    const next = [...sections];
    const target = index + direction;
    if (target < 0 || target >= next.length) return;
    [next[index], next[target]] = [next[target], next[index]];

    startTransition(async () => {
      try {
        await reorderSections(
          courseId,
          next.map((s) => s.id).filter((id): id is string => Boolean(id)),
        );
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  return (
    <div className="space-y-3">
      {sections.length === 0 ? (
        <p className="text-muted-foreground text-sm">No sections yet.</p>
      ) : (
        <ul className="space-y-2">
          {sections.map((section, index) => (
            <li key={section.id} className="flex items-center gap-2 rounded-lg border px-3 py-2">
              <ReorderButtons
                disabled={isPending}
                disableUp={index === 0}
                disableDown={index === sections.length - 1}
                onMoveUp={() => move(index, -1)}
                onMoveDown={() => move(index, 1)}
              />
              <Link href={`${basePath}/sections/${section.id}`} className="flex-1 hover:underline">
                {section.title}
              </Link>
              {section.status && <Badge variant={STATUS_VARIANT[section.status]}>{section.status}</Badge>}
              <span className="text-muted-foreground text-xs">{section.lessons?.length ?? 0} lessons</span>
            </li>
          ))}
        </ul>
      )}

      <Link href={`${basePath}/sections/new`} className={cn(buttonVariants({ variant: "outline", size: "sm" }))}>
        Add section
      </Link>
    </div>
  );
}
