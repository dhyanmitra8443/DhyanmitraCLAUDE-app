"use client";

import { useRouter } from "next/navigation";
import { useTransition } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { updatePlanStatus } from "@/lib/subscriptions/client";

/** Ref: SRS 9.5, 9.15 - existing purchased subscriptions remain valid regardless of later plan status changes. */
export function SubscriptionPlanStatusAction({
  planId,
  status,
}: {
  planId: string;
  status: "ACTIVE" | "INACTIVE" | "ARCHIVED";
}) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  function setStatus(next: "ACTIVE" | "INACTIVE" | "ARCHIVED") {
    startTransition(async () => {
      try {
        await updatePlanStatus(planId, next);
        toast.success(`Plan ${next.toLowerCase()}.`);
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  if (status === "ARCHIVED") return null;

  return (
    <div className="flex gap-2">
      {status === "ACTIVE" ? (
        <Button size="sm" variant="secondary" disabled={isPending} onClick={() => setStatus("INACTIVE")}>
          Deactivate
        </Button>
      ) : (
        <Button size="sm" disabled={isPending} onClick={() => setStatus("ACTIVE")}>
          Activate
        </Button>
      )}
      <Button size="sm" variant="destructive" disabled={isPending} onClick={() => setStatus("ARCHIVED")}>
        Archive
      </Button>
    </div>
  );
}
