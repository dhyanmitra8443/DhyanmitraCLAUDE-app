import Link from "next/link";
import { notFound } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ReportFilterBar } from "./report-filter-bar";
import { ReportTable } from "./report-table";
import { ExportReportButton } from "./export-report-button";
import { PaginationControls } from "@/components/shared/pagination-controls";
import { getReportData, listAvailableReports } from "@/lib/reports/queries";
import { ApiError } from "@/lib/api/errors";

/**
 * Ref: SRS 15.4-15.11 - shared viewer for any reportKey, reused by all three
 * portals (only the "back to reports" basePath differs per role).
 */
export async function ReportViewer({
  reportKey,
  basePath,
  searchParams,
}: {
  reportKey: string;
  basePath: string;
  searchParams: Record<string, string | string[] | undefined>;
}) {
  const page = Number(searchParams.page ?? 0);
  const dateFrom = typeof searchParams.dateFrom === "string" ? searchParams.dateFrom : undefined;
  const dateTo = typeof searchParams.dateTo === "string" ? searchParams.dateTo : undefined;
  const courseId = typeof searchParams.courseId === "string" ? searchParams.courseId : undefined;
  const search = typeof searchParams.search === "string" ? searchParams.search : undefined;

  let data;
  let definitions;
  try {
    [data, definitions] = await Promise.all([
      getReportData(reportKey, { page, size: 20, dateFrom, dateTo, courseId, search }),
      listAvailableReports(),
    ]);
  } catch (error) {
    if (error instanceof ApiError && (error.status === 403 || error.status === 404)) notFound();
    throw error;
  }

  const title = definitions.find((d) => d.reportKey === reportKey)?.title ?? reportKey;
  const viewerPath = `${basePath}/${reportKey}`;

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <Link href={basePath} className="text-muted-foreground text-sm hover:underline">
            ← Back to reports
          </Link>
          <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
        </div>
        <ExportReportButton reportKey={reportKey} filters={{ dateFrom, dateTo, courseId, search }} />
      </div>

      <ReportFilterBar basePath={viewerPath} />

      <Card>
        <CardHeader>
          <CardTitle className="text-muted-foreground text-sm font-normal">
            {data.content.length} row{data.content.length === 1 ? "" : "s"} on this page · generated{" "}
            {new Date(data.generatedAt).toLocaleString()}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <ReportTable columns={data.columns} rows={data.content} />
        </CardContent>
      </Card>

      <PaginationControls page={data.page.page ?? 0} totalPages={data.page.totalPages ?? 1} />
    </div>
  );
}
