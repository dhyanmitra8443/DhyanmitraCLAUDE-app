import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { OrdersFilterBar } from "@/components/payments/orders-filter-bar";
import { PaginationControls } from "@/components/shared/pagination-controls";
import { searchOrders } from "@/lib/payments/queries";
import { formatCurrency } from "@/lib/format";
import type { OrderStatus } from "@/lib/api/types";

export const metadata = { title: "Orders | Dhyan Mitra" };

const STATUS_VARIANT = {
  PENDING: "outline",
  PAID: "default",
  FAILED: "destructive",
  CANCELLED: "secondary",
} as const;

/** Ref: SRS 10.15 - administrator-only order search across all students. */
export default async function AdminOrdersPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const page = Number(params.page ?? 0);
  const studentName = typeof params.studentName === "string" ? params.studentName : undefined;
  const status = typeof params.status === "string" ? (params.status as OrderStatus) : undefined;

  const result = await searchOrders({ page, size: 20, sort: "createdAt,desc", studentName, status });

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Orders</h1>

      <OrdersFilterBar />

      <Card>
        <CardContent>
          {result.content.length === 0 ? (
            <p className="text-muted-foreground text-sm">No orders match these filters.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Course</TableHead>
                  <TableHead>Student ID</TableHead>
                  <TableHead>Amount</TableHead>
                  <TableHead>Created</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {result.content.map((order) => (
                  <TableRow key={order.id}>
                    <TableCell className="font-medium">
                      <Link href={`/admin/orders/${order.id}`} className="hover:underline">
                        {order.course?.title ?? "Course"}
                      </Link>
                    </TableCell>
                    <TableCell className="text-muted-foreground font-mono text-xs">{order.studentId}</TableCell>
                    <TableCell>{order.amount != null ? formatCurrency(order.amount) : "—"}</TableCell>
                    <TableCell>{order.createdAt ? new Date(order.createdAt).toLocaleString() : "—"}</TableCell>
                    <TableCell>
                      {order.status && <Badge variant={STATUS_VARIANT[order.status]}>{order.status}</Badge>}
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
