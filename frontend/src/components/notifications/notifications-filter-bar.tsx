"use client";

import { useRouter, useSearchParams } from "next/navigation";

const selectClassName =
  "h-8 rounded-lg border border-input bg-transparent px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30";

/** Ref: SRS 14.4 - filter the inbox by read status. */
export function NotificationsFilterBar({ basePath }: { basePath: string }) {
  const router = useRouter();
  const searchParams = useSearchParams();

  function updateParams(readStatus: string | null) {
    const params = new URLSearchParams(searchParams.toString());
    if (readStatus) params.set("readStatus", readStatus);
    else params.delete("readStatus");
    params.delete("page");
    router.push(`${basePath}?${params.toString()}`);
  }

  return (
    <select
      className={selectClassName}
      value={searchParams.get("readStatus") ?? ""}
      onChange={(e) => updateParams(e.target.value || null)}
    >
      <option value="">All</option>
      <option value="UNREAD">Unread</option>
      <option value="READ">Read</option>
    </select>
  );
}
