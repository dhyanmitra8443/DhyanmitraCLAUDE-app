import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { LiveClassesFilterBar } from "@/components/live-classes/live-classes-filter-bar";
import { PaginationControls } from "@/components/shared/pagination-controls";
import { searchLiveClasses } from "@/lib/live-classes/queries";
import type { LiveClassStatus } from "@/lib/api/types";

export const metadata = { title: "Live classes | Dhyan Mitra" };

const STATUS_VARIANT = {
  SCHEDULED: "default",
  CANCELLED: "destructive",
  COMPLETED: "secondary",
} as const;

export default async function InstructorLiveClassesPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const page = Number(params.page ?? 0);
  const courseId = typeof params.courseId === "string" ? params.courseId : undefined;
  const status = typeof params.status === "string" ? (params.status as LiveClassStatus) : undefined;
  const date = typeof params.date === "string" ? params.date : undefined;

  const result = await searchLiveClasses({ page, size: 20, sort: "scheduledDate,desc", courseId, status, date });

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Live classes</h1>

      <LiveClassesFilterBar basePath="/teach/live-classes" />

      <Card>
        <CardContent>
          {result.content.length === 0 ? (
            <p className="text-muted-foreground text-sm">No live classes match these filters.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Title</TableHead>
                  <TableHead>Date</TableHead>
                  <TableHead>Time</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {result.content.map((liveClass) => (
                  <TableRow key={liveClass.id}>
                    <TableCell className="font-medium">
                      <Link href={`/teach/live-classes/${liveClass.id}`} className="hover:underline">
                        {liveClass.title}
                      </Link>
                    </TableCell>
                    <TableCell>{liveClass.scheduledDate}</TableCell>
                    <TableCell>{liveClass.scheduledTime}</TableCell>
                    <TableCell>
                      {liveClass.status && <Badge variant={STATUS_VARIANT[liveClass.status]}>{liveClass.status}</Badge>}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <PaginationControls page={result.page.page ?? 0} totalPages={result.page.totalPages ?? 1} />
    </div>
  );
}
