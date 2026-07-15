"use client";

import { useState, useTransition } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { ApiError } from "@/lib/api/errors";
import { assignInstructor, removeInstructor } from "@/lib/courses/client";
import type { UserSummary } from "@/lib/api/types";

const selectClassName =
  "h-8 rounded-lg border border-input bg-transparent px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30";

/** Ref: SRS 5.6, 4.8 - administrator-only; enforces "can't drop the last instructor from a published course" server-side. */
export function CourseInstructorsManager({
  courseId,
  current,
  available,
}: {
  courseId: string;
  current: UserSummary[];
  available: UserSummary[];
}) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();
  const [selected, setSelected] = useState("");

  const currentIds = new Set(current.map((i) => i.id));
  const options = available.filter((i) => i.id && !currentIds.has(i.id));

  function handleAdd() {
    if (!selected) return;
    startTransition(async () => {
      try {
        await assignInstructor(courseId, selected);
        setSelected("");
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  function handleRemove(instructorId: string) {
    startTransition(async () => {
      try {
        await removeInstructor(courseId, instructorId);
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  return (
    <div className="space-y-3">
      <div className="flex flex-wrap gap-2">
        {current.length === 0 && <p className="text-muted-foreground text-sm">No instructors assigned.</p>}
        {current.map(
          (instructor) =>
            instructor.id && (
              <Badge key={instructor.id} variant="secondary" className="gap-1">
                {instructor.firstName} {instructor.lastName}
                <button
                  type="button"
                  disabled={isPending}
                  onClick={() => handleRemove(instructor.id!)}
                  className="text-muted-foreground hover:text-foreground"
                  aria-label={`Remove ${instructor.firstName}`}
                >
                  ×
                </button>
              </Badge>
            ),
        )}
      </div>

      {options.length > 0 && (
        <div className="flex gap-2">
          <select className={selectClassName} value={selected} onChange={(e) => setSelected(e.target.value)}>
            <option value="">Add an instructor…</option>
            {options.map((instructor) => (
              <option key={instructor.id} value={instructor.id}>
                {instructor.firstName} {instructor.lastName}
              </option>
            ))}
          </select>
          <button
            type="button"
            disabled={!selected || isPending}
            onClick={handleAdd}
            className="rounded-lg border border-input px-3 text-sm disabled:opacity-50"
          >
            Add
          </button>
        </div>
      )}
    </div>
  );
}
