import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { SubscriptionsFilterBar } from "@/components/subscriptions/subscriptions-filter-bar";
import { PaginationControls } from "@/components/shared/pagination-controls";
import { searchSubscriptions } from "@/lib/subscriptions/queries";
import type { SubscriptionStatus } from "@/lib/api/types";

export const metadata = { title: "Subscriptions | Dhyan Mitra" };

const STATUS_VARIANT = {
  ACTIVE: "default",
  EXPIRED: "secondary",
  CANCELLED: "destructive",
  PENDING: "outline",
} as const;

export default async function AdminSubscriptionsPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const page = Number(params.page ?? 0);
  const studentName = typeof params.studentName === "string" ? params.studentName : undefined;
  const courseId = typeof params.courseId === "string" ? params.courseId : undefined;
  const status = typeof params.status === "string" ? (params.status as SubscriptionStatus) : undefined;

  const result = await searchSubscriptions({ page, size: 20, sort: "purchaseDate,desc", studentName, courseId, status });

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Subscriptions</h1>

      <SubscriptionsFilterBar />

      <Card>
        <CardContent>
          {result.content.length === 0 ? (
            <p className="text-muted-foreground text-sm">No subscriptions match these filters.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Course</TableHead>
                  <TableHead>Student ID</TableHead>
                  <TableHead>Start</TableHead>
                  <TableHead>End</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {result.content.map((subscription) => (
                  <TableRow key={subscription.id}>
                    <TableCell className="font-medium">
                      <Link href={`/admin/subscriptions/${subscription.id}`} className="hover:underline">
                        {subscription.course?.title}
                      </Link>
                    </TableCell>
                    <TableCell className="text-muted-foreground font-mono text-xs">
                      {subscription.studentId}
                    </TableCell>
                    <TableCell>{subscription.startDate}</TableCell>
                    <TableCell>{subscription.endDate}</TableCell>
                    <TableCell>
                      {subscription.status && (
                        <Badge variant={STATUS_VARIANT[subscription.status]}>{subscription.status}</Badge>
                      )}
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
