"use client";

import { useState, useTransition } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { ApiError } from "@/lib/api/errors";
import { assignCategories, removeCategory } from "@/lib/courses/client";
import type { CategorySummary } from "@/lib/api/types";

const selectClassName =
  "h-8 rounded-lg border border-input bg-transparent px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30";

/** Ref: SRS 5.7 - add/remove goes through the dedicated endpoints so "can't drop the last category from a published course" is enforced server-side. */
export function CourseCategoriesManager({
  courseId,
  current,
  available,
}: {
  courseId: string;
  current: CategorySummary[];
  available: CategorySummary[];
}) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();
  const [selected, setSelected] = useState("");

  const currentIds = new Set(current.map((c) => c.id));
  const options = available.filter((c) => c.id && !currentIds.has(c.id));

  function handleAdd() {
    if (!selected) return;
    startTransition(async () => {
      try {
        await assignCategories(courseId, [selected]);
        setSelected("");
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  function handleRemove(categoryId: string) {
    startTransition(async () => {
      try {
        await removeCategory(courseId, categoryId);
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  return (
    <div className="space-y-3">
      <div className="flex flex-wrap gap-2">
        {current.length === 0 && <p className="text-muted-foreground text-sm">No categories assigned.</p>}
        {current.map(
          (category) =>
            category.id && (
              <Badge key={category.id} variant="secondary" className="gap-1">
                {category.name}
                <button
                  type="button"
                  disabled={isPending}
                  onClick={() => handleRemove(category.id!)}
                  className="text-muted-foreground hover:text-foreground"
                  aria-label={`Remove ${category.name}`}
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
            <option value="">Add a category…</option>
            {options.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
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
