import Link from "next/link";
import { notFound } from "next/navigation";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getOrderDetail } from "@/lib/payments/queries";
import { formatCurrency } from "@/lib/format";
import { ApiError } from "@/lib/api/errors";

export const metadata = { title: "Order details | Dhyan Mitra" };

const ORDER_STATUS_VARIANT = {
  PENDING: "outline",
  PAID: "default",
  FAILED: "destructive",
  CANCELLED: "secondary",
} as const;

/** Ref: SRS 10.4 - administrator, or the owning student. */
export default async function OrderDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  let order;
  try {
    order = await getOrderDetail(id);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) notFound();
    throw error;
  }

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <Link href="/learn/payments" className="text-muted-foreground text-sm hover:underline">
        ← Back to payments
      </Link>

      <div className="flex items-center justify-between gap-4">
        <h1 className="text-2xl font-semibold tracking-tight">{order.course?.title ?? "Order"}</h1>
        {order.status && <Badge variant={ORDER_STATUS_VARIANT[order.status]}>{order.status}</Badge>}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Order details</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-muted-foreground">Amount</span>
            <span>{order.amount != null ? formatCurrency(order.amount) : "—"}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Order ID</span>
            <span className="font-mono text-xs">{order.id}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Created</span>
            <span>{order.createdAt ? new Date(order.createdAt).toLocaleString() : "—"}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Last updated</span>
            <span>{order.updatedAt ? new Date(order.updatedAt).toLocaleString() : "—"}</span>
          </div>
        </CardContent>
      </Card>

      {order.courseId && (
        <Link href={`/courses/${order.courseId}`} className="text-sm underline underline-offset-2">
          View course
        </Link>
      )}
    </div>
  );
}
