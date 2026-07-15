"use client";

import { useRouter } from "next/navigation";
import { useTransition } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { updateUserStatus } from "@/lib/users/client";

/** Ref: SRS 4.4, 4.5 - block/activate is the only status change an administrator can make. */
export function UserStatusAction({
  userId,
  status,
}: {
  userId: string;
  status: "ACTIVE" | "INACTIVE" | "BLOCKED";
}) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  function setStatus(next: "ACTIVE" | "BLOCKED") {
    startTransition(async () => {
      try {
        await updateUserStatus(userId, next);
        toast.success(next === "BLOCKED" ? "User blocked." : "User activated.");
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  if (status === "ACTIVE") {
    return (
      <Button variant="destructive" size="sm" disabled={isPending} onClick={() => setStatus("BLOCKED")}>
        {isPending ? "Blocking…" : "Block user"}
      </Button>
    );
  }

  return (
    <Button size="sm" disabled={isPending} onClick={() => setStatus("ACTIVE")}>
      {isPending ? "Activating…" : "Activate user"}
    </Button>
  );
}
