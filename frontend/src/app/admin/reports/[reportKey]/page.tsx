import { ReportViewer } from "@/components/reports/report-viewer";

export const metadata = { title: "Report | Dhyan Mitra" };

export default async function AdminReportViewerPage({
  params,
  searchParams,
}: {
  params: Promise<{ reportKey: string }>;
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const { reportKey } = await params;
  const resolvedSearchParams = await searchParams;

  return <ReportViewer reportKey={reportKey} basePath="/admin/reports" searchParams={resolvedSearchParams} />;
}
