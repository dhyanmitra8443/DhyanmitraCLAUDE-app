import Link from "next/link";
import { Card, CardContent } from "@/components/ui/card";
import type { ReportDefinition } from "@/lib/api/types";

/** Ref: SRS 15.3 - the reports available to the caller's role. */
export function ReportsList({ reports, basePath }: { reports: ReportDefinition[]; basePath: string }) {
  if (reports.length === 0) {
    return <p className="text-muted-foreground text-sm">No reports available.</p>;
  }

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
      {reports.map((report) => (
        <Link key={report.reportKey} href={`${basePath}/${report.reportKey}`}>
          <Card className="hover:bg-accent/50 transition-colors">
            <CardContent className="py-4">
              <p className="text-sm font-medium">{report.title}</p>
            </CardContent>
          </Card>
        </Link>
      ))}
    </div>
  );
}
