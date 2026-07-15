import { ReportsList } from "@/components/reports/reports-list";
import { listAvailableReports } from "@/lib/reports/queries";

export const metadata = { title: "Reports | Dhyan Mitra" };

export default async function AdminReportsPage() {
  const reports = await listAvailableReports();

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Reports</h1>
      <ReportsList reports={reports} basePath="/admin/reports" />
    </div>
  );
}
