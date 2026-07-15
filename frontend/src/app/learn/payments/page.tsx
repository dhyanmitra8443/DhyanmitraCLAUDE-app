import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { listMyOrders, listMyPayments } from "@/lib/payments/queries";
import { formatCurrency } from "@/lib/format";

export const metadata = { title: "Payments | Dhyan Mitra" };

const ORDER_STATUS_VARIANT = {
  PENDING: "outline",
  PAID: "default",
  FAILED: "destructive",
  CANCELLED: "secondary",
} as const;

const PAYMENT_STATUS_VARIANT = {
  PENDING: "outline",
  SUCCESS: "default",
  FAILED: "destructive",
  CANCELLED: "secondary",
} as const;

/** Ref: SRS 10.13, 10.15 - the authenticated student's own order + payment history. */
export default async function MyPaymentsPage() {
  const [orders, payments] = await Promise.all([listMyOrders({ size: 50 }), listMyPayments({ size: 50 })]);

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Payments</h1>

      <Card>
        <CardHeader>
          <CardTitle>Order history</CardTitle>
        </CardHeader>
        <CardContent>
          {orders.content.length === 0 ? (
            <p className="text-muted-foreground text-sm">
              No orders yet.{" "}
              <Link href="/courses" className="underline underline-offset-2">
                Browse courses
              </Link>
              .
            </p>
          ) : (
            <ul className="space-y-2">
              {orders.content.map((order) => (
                <li key={order.id} className="flex items-center justify-between gap-4 rounded-lg border px-3 py-2">
                  <div>
                    <Link href={`/learn/orders/${order.id}`} className="text-sm font-medium hover:underline">
                      {order.course?.title ?? "Course"}
                    </Link>
                    <p className="text-muted-foreground text-xs">
                      {order.amount != null ? formatCurrency(order.amount) : ""}
                      {order.createdAt ? ` · ${new Date(order.createdAt).toLocaleString()}` : ""}
                    </p>
                  </div>
                  {order.status && <Badge variant={ORDER_STATUS_VARIANT[order.status]}>{order.status}</Badge>}
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Payment history</CardTitle>
        </CardHeader>
        <CardContent>
          {payments.content.length === 0 ? (
            <p className="text-muted-foreground text-sm">No payments yet.</p>
          ) : (
            <ul className="space-y-2">
              {payments.content.map((payment) => (
                <li key={payment.id} className="flex items-center justify-between gap-4 rounded-lg border px-3 py-2">
                  <div>
                    <Link href={`/learn/orders/${payment.orderId}`} className="text-sm font-medium hover:underline">
                      {payment.transactionReference ?? "Payment"}
                    </Link>
                    <p className="text-muted-foreground text-xs">
                      {payment.amount != null ? formatCurrency(payment.amount) : ""}
                      {payment.paymentMethod ? ` · ${payment.paymentMethod.replace("_", " ")}` : ""}
                      {payment.paymentDate ? ` · ${new Date(payment.paymentDate).toLocaleString()}` : ""}
                    </p>
                  </div>
                  {payment.status && <Badge variant={PAYMENT_STATUS_VARIANT[payment.status]}>{payment.status}</Badge>}
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
