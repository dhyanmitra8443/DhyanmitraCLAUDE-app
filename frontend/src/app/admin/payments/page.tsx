import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { PaymentsFilterBar } from "@/components/payments/payments-filter-bar";
import { PaginationControls } from "@/components/shared/pagination-controls";
import { searchPayments } from "@/lib/payments/queries";
import { formatCurrency } from "@/lib/format";
import type { PaymentStatus } from "@/lib/api/types";

export const metadata = { title: "Payments | Dhyan Mitra" };

const STATUS_VARIANT = {
  PENDING: "outline",
  SUCCESS: "default",
  FAILED: "destructive",
  CANCELLED: "secondary",
} as const;

/** Ref: SRS 10.16 - administrator-only payment search across all students. */
export default async function AdminPaymentsPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const page = Number(params.page ?? 0);
  const status = typeof params.status === "string" ? (params.status as PaymentStatus) : undefined;
  const transactionReference =
    typeof params.transactionReference === "string" ? params.transactionReference : undefined;

  const result = await searchPayments({ page, size: 20, sort: "paymentDate,desc", status, transactionReference });

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Payments</h1>

      <PaymentsFilterBar />

      <Card>
        <CardContent>
          {result.content.length === 0 ? (
            <p className="text-muted-foreground text-sm">No payments match these filters.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Transaction reference</TableHead>
                  <TableHead>Student ID</TableHead>
                  <TableHead>Amount</TableHead>
                  <TableHead>Method</TableHead>
                  <TableHead>Payment date</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {result.content.map((payment) => (
                  <TableRow key={payment.id}>
                    <TableCell className="font-medium">
                      <Link href={`/admin/payments/${payment.id}`} className="hover:underline">
                        {payment.transactionReference ?? payment.id}
                      </Link>
                    </TableCell>
                    <TableCell className="text-muted-foreground font-mono text-xs">{payment.studentId}</TableCell>
                    <TableCell>{payment.amount != null ? formatCurrency(payment.amount) : "—"}</TableCell>
                    <TableCell>{payment.paymentMethod?.replace("_", " ") ?? "—"}</TableCell>
                    <TableCell>
                      {payment.paymentDate ? new Date(payment.paymentDate).toLocaleString() : "—"}
                    </TableCell>
                    <TableCell>
                      {payment.status && <Badge variant={STATUS_VARIANT[payment.status]}>{payment.status}</Badge>}
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
