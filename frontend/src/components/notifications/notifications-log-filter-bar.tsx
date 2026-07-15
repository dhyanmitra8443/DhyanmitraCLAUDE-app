"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

const selectClassName =
  "h-8 rounded-lg border border-input bg-transparent px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30";

/** Ref: SRS 14.13 - administrator-only system-wide log search. */
export function NotificationsLogFilterBar() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [userId, setUserId] = useState(searchParams.get("userId") ?? "");
  const [notificationType, setNotificationType] = useState(searchParams.get("notificationType") ?? "");

  function updateParams(updates: Record<string, string | null>) {
    const params = new URLSearchParams(searchParams.toString());
    for (const [key, value] of Object.entries(updates)) {
      if (value) params.set(key, value);
      else params.delete(key);
    }
    params.delete("page");
    router.push(`/admin/notifications/log?${params.toString()}`);
  }

  return (
    <form
      className="flex flex-wrap items-center gap-2"
      onSubmit={(e) => {
        e.preventDefault();
        updateParams({ userId: userId || null, notificationType: notificationType || null });
      }}
    >
      <Input value={userId} onChange={(e) => setUserId(e.target.value)} placeholder="User ID" className="max-w-xs" />
      <Input
        value={notificationType}
        onChange={(e) => setNotificationType(e.target.value)}
        placeholder="Related module"
        className="max-w-xs"
      />
      <Button type="submit" variant="outline" size="sm">
        Search
      </Button>

      <select
        className={selectClassName}
        value={searchParams.get("deliveryChannel") ?? ""}
        onChange={(e) => updateParams({ deliveryChannel: e.target.value || null })}
      >
        <option value="">All channels</option>
        <option value="IN_APP">IN_APP</option>
        <option value="EMAIL">EMAIL</option>
      </select>
    </form>
  );
}
