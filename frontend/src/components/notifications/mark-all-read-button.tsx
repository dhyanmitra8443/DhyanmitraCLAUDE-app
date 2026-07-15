"use client";

import { useRouter } from "next/navigation";
import { useTransition } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { markAllNotificationsRead } from "@/lib/notifications/client";

export function MarkAllReadButton() {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  function onClick() {
    startTransition(async () => {
      try {
        await markAllNotificationsRead();
        toast.success("All notifications marked as read.");
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  return (
    <Button size="sm" variant="outline" disabled={isPending} onClick={onClick}>
      {isPending ? "Saving…" : "Mark all read"}
    </Button>
  );
}
