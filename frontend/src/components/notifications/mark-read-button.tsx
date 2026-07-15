"use client";

import { useRouter } from "next/navigation";
import { useTransition } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { markNotificationRead } from "@/lib/notifications/client";

export function MarkReadButton({ notificationId }: { notificationId: string }) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  function onClick() {
    startTransition(async () => {
      try {
        await markNotificationRead(notificationId);
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  return (
    <Button size="sm" variant="outline" disabled={isPending} onClick={onClick}>
      {isPending ? "Saving…" : "Mark read"}
    </Button>
  );
}
