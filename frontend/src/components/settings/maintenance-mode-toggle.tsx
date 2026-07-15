"use client";

import { useRouter } from "next/navigation";
import { useTransition } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { updateMaintenanceMode } from "@/lib/settings/client";

/** Ref: SRS 16.12 - while enabled, only administrators may access the system. */
export function MaintenanceModeToggle({ enabled }: { enabled: boolean }) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  function handleToggle() {
    startTransition(async () => {
      try {
        await updateMaintenanceMode(!enabled);
        toast.success(enabled ? "Maintenance mode disabled." : "Maintenance mode enabled.");
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  return (
    <div className="flex items-center justify-between gap-4">
      <p className="text-sm">
        Status:{" "}
        <span className={enabled ? "font-medium text-destructive" : "text-muted-foreground"}>
          {enabled ? "Enabled — only administrators can access the system" : "Disabled"}
        </span>
      </p>
      <Button size="sm" variant={enabled ? "outline" : "destructive"} disabled={isPending} onClick={handleToggle}>
        {isPending ? "Saving…" : enabled ? "Disable maintenance mode" : "Enable maintenance mode"}
      </Button>
    </div>
  );
}
