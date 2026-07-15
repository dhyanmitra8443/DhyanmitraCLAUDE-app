import Link from "next/link";
import { notFound } from "next/navigation";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getPaymentDetail } from "@/lib/payments/queries";
import { formatCurrency } from "@/lib/format";
import { ApiError } from "@/lib/api/errors";

export const metadata = { title: "Payment details | Dhyan Mitra" };

const STATUS_VARIANT = {
  PENDING: "outline",
  SUCCESS: "default",
  FAILED: "destructive",
  CANCELLED: "secondary",
} as const;

/** Ref: SRS 10.6 - administrator, or the owning student. */
export default async function AdminPaymentDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  let payment;
  try {
    payment = await getPaymentDetail(id);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) notFound();
    throw error;
  }

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <Link href="/admin/payments" className="text-muted-foreground text-sm hover:underline">
        ← Back to payments
      </Link>

      <div className="flex items-center justify-between gap-4">
        <h1 className="text-2xl font-semibold tracking-tight">{payment.transactionReference ?? "Payment"}</h1>
        {payment.status && <Badge variant={STATUS_VARIANT[payment.status]}>{payment.status}</Badge>}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Payment details</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-muted-foreground">Payment ID</span>
            <span className="font-mono text-xs">{payment.id}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Student ID</span>
            <span className="font-mono text-xs">{payment.studentId}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Amount</span>
            <span>{payment.amount != null ? formatCurrency(payment.amount) : "—"}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Method</span>
            <span>{payment.paymentMethod?.replace("_", " ") ?? "—"}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Transaction reference</span>
            <span className="font-mono text-xs">{payment.transactionReference ?? "—"}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Payment date</span>
            <span>{payment.paymentDate ? new Date(payment.paymentDate).toLocaleString() : "—"}</span>
          </div>
        </CardContent>
      </Card>

      {payment.orderId && (
        <Link href={`/admin/orders/${payment.orderId}`} className="text-sm underline underline-offset-2">
          View order
        </Link>
      )}
    </div>
  );
}
