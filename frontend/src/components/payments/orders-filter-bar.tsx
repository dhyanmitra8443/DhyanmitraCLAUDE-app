"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

const selectClassName =
  "h-8 rounded-lg border border-input bg-transparent px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30";

/** Ref: SRS 10.15 - search by student name, status (course/order-id filters aren't backed by the search endpoint). */
export function OrdersFilterBar() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [studentName, setStudentName] = useState(searchParams.get("studentName") ?? "");

  function updateParams(updates: Record<string, string | null>) {
    const params = new URLSearchParams(searchParams.toString());
    for (const [key, value] of Object.entries(updates)) {
      if (value) params.set(key, value);
      else params.delete(key);
    }
    params.delete("page");
    router.push(`/admin/orders?${params.toString()}`);
  }

  return (
    <form
      className="flex flex-wrap items-center gap-2"
      onSubmit={(e) => {
        e.preventDefault();
        updateParams({ studentName: studentName || null });
      }}
    >
      <Input
        value={studentName}
        onChange={(e) => setStudentName(e.target.value)}
        placeholder="Student name"
        className="max-w-xs"
      />
      <Button type="submit" variant="outline" size="sm">
        Search
      </Button>

      <select
        className={selectClassName}
        value={searchParams.get("status") ?? ""}
        onChange={(e) => updateParams({ status: e.target.value || null })}
      >
        <option value="">All statuses</option>
        <option value="PENDING">PENDING</option>
        <option value="PAID">PAID</option>
        <option value="FAILED">FAILED</option>
        <option value="CANCELLED">CANCELLED</option>
      </select>
    </form>
  );
}
