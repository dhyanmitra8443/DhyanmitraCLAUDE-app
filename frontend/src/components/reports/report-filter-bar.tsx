"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

/** Ref: SRS 15.7, 15.10 - the same generic filter set every reportKey accepts. */
export function ReportFilterBar({ basePath }: { basePath: string }) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [dateFrom, setDateFrom] = useState(searchParams.get("dateFrom") ?? "");
  const [dateTo, setDateTo] = useState(searchParams.get("dateTo") ?? "");
  const [courseId, setCourseId] = useState(searchParams.get("courseId") ?? "");
  const [search, setSearch] = useState(searchParams.get("search") ?? "");

  return (
    <form
      className="flex flex-wrap items-center gap-2"
      onSubmit={(e) => {
        e.preventDefault();
        const params = new URLSearchParams();
        if (dateFrom) params.set("dateFrom", dateFrom);
        if (dateTo) params.set("dateTo", dateTo);
        if (courseId) params.set("courseId", courseId);
        if (search) params.set("search", search);
        router.push(`${basePath}?${params.toString()}`);
      }}
    >
      <Input type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} className="max-w-xs" />
      <Input type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} className="max-w-xs" />
      <Input value={courseId} onChange={(e) => setCourseId(e.target.value)} placeholder="Course ID" className="max-w-xs" />
      <Input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search" className="max-w-xs" />
      <Button type="submit" variant="outline" size="sm">
        Apply filters
      </Button>
    </form>
  );
}
