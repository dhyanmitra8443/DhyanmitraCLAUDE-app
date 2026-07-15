"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

const selectClassName =
  "h-8 rounded-lg border border-input bg-transparent px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30";

/** Ref: SRS 9.16 - search by student name, course, status. */
export function SubscriptionsFilterBar() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [studentName, setStudentName] = useState(searchParams.get("studentName") ?? "");
  const [courseId, setCourseId] = useState(searchParams.get("courseId") ?? "");

  function updateParams(updates: Record<string, string | null>) {
    const params = new URLSearchParams(searchParams.toString());
    for (const [key, value] of Object.entries(updates)) {
      if (value) params.set(key, value);
      else params.delete(key);
    }
    params.delete("page");
    router.push(`/admin/subscriptions?${params.toString()}`);
  }

  return (
    <form
      className="flex flex-wrap items-center gap-2"
      onSubmit={(e) => {
        e.preventDefault();
        updateParams({ studentName: studentName || null, courseId: courseId || null });
      }}
    >
      <Input
        value={studentName}
        onChange={(e) => setStudentName(e.target.value)}
        placeholder="Student name"
        className="max-w-xs"
      />
      <Input
        value={courseId}
        onChange={(e) => setCourseId(e.target.value)}
        placeholder="Course ID"
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
        <option value="ACTIVE">ACTIVE</option>
        <option value="EXPIRED">EXPIRED</option>
        <option value="CANCELLED">CANCELLED</option>
        <option value="PENDING">PENDING</option>
      </select>
    </form>
  );
}
