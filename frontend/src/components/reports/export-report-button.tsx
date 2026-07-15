"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { exportReport, type ReportFilterParams } from "@/lib/reports/client";
import type { ReportFormat } from "@/lib/api/types";

const FORMATS: ReportFormat[] = ["CSV", "XLSX", "PDF"];

/** Ref: SRS 15.9, 15.15 - exports reflect the same filters as the report currently on screen. */
export function ExportReportButton({ reportKey, filters }: { reportKey: string; filters: ReportFilterParams }) {
  const [pendingFormat, setPendingFormat] = useState<ReportFormat | null>(null);

  async function onExport(format: ReportFormat) {
    setPendingFormat(format);
    try {
      await exportReport(reportKey, format, filters);
    } catch (error) {
      toast.error(error instanceof ApiError ? error.message : "Export failed. Please try again.");
    } finally {
      setPendingFormat(null);
    }
  }

  return (
    <div className="flex gap-2">
      {FORMATS.map((format) => (
        <Button
          key={format}
          size="sm"
          variant="outline"
          disabled={pendingFormat !== null}
          onClick={() => onExport(format)}
        >
          {pendingFormat === format ? "Exporting…" : `Export ${format}`}
        </Button>
      ))}
    </div>
  );
}
