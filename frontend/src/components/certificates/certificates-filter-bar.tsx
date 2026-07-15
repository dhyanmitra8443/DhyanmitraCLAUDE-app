"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

/** Ref: SRS 12.14 - search by student name, course, certificate number. */
export function CertificatesFilterBar() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [studentName, setStudentName] = useState(searchParams.get("studentName") ?? "");
  const [courseId, setCourseId] = useState(searchParams.get("courseId") ?? "");
  const [certificateNumber, setCertificateNumber] = useState(searchParams.get("certificateNumber") ?? "");

  return (
    <form
      className="flex flex-wrap items-center gap-2"
      onSubmit={(e) => {
        e.preventDefault();
        const params = new URLSearchParams();
        if (studentName) params.set("studentName", studentName);
        if (courseId) params.set("courseId", courseId);
        if (certificateNumber) params.set("certificateNumber", certificateNumber);
        router.push(`/admin/certificates?${params.toString()}`);
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
      <Input
        value={certificateNumber}
        onChange={(e) => setCertificateNumber(e.target.value)}
        placeholder="Certificate number"
        className="max-w-xs"
      />
      <Button type="submit" variant="outline" size="sm">
        Search
      </Button>
    </form>
  );
}
