"use client";

import { useRouter } from "next/navigation";
import { useTransition } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { cancelLiveClass } from "@/lib/live-classes/client";

/** Ref: SRS 11.13 - administrator or assigned instructor cancels a scheduled live class. */
export function LiveClassCancelAction({ liveClassId }: { liveClassId: string }) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  function onCancel() {
    if (!confirm("Cancel this live class? Students will no longer be able to join.")) return;
    startTransition(async () => {
      try {
        await cancelLiveClass(liveClassId);
        toast.success("Live class cancelled.");
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  return (
    <Button size="sm" variant="destructive" disabled={isPending} onClick={onCancel}>
      Cancel class
    </Button>
  );
}
