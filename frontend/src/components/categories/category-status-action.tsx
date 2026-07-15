"use client";

import { useRouter } from "next/navigation";
import { useTransition } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { updateCategoryStatus } from "@/lib/categories/client";

/** Ref: SRS 6.4, 6.8 - categories are never deleted, only (de)activated. */
export function CategoryStatusAction({ categoryId, status }: { categoryId: string; status: "ACTIVE" | "INACTIVE" }) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  function toggle() {
    const next = status === "ACTIVE" ? "INACTIVE" : "ACTIVE";
    startTransition(async () => {
      try {
        await updateCategoryStatus(categoryId, next);
        toast.success(next === "INACTIVE" ? "Category deactivated." : "Category activated.");
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  return (
    <Button size="sm" variant={status === "ACTIVE" ? "destructive" : "default"} disabled={isPending} onClick={toggle}>
      {isPending ? "Updating…" : status === "ACTIVE" ? "Deactivate" : "Activate"}
    </Button>
  );
}
