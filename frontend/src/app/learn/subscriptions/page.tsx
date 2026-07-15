import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { getMySubscriptions } from "@/lib/subscriptions/queries";

export const metadata = { title: "My subscriptions | Dhyan Mitra" };

const STATUS_VARIANT = {
  ACTIVE: "default",
  EXPIRED: "secondary",
  CANCELLED: "destructive",
  PENDING: "outline",
} as const;

export default async function MySubscriptionsPage() {
  const subscriptions = await getMySubscriptions();

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">My subscriptions</h1>

      <Card>
        <CardContent>
          {subscriptions.length === 0 ? (
            <p className="text-muted-foreground text-sm">
              No subscriptions yet.{" "}
              <Link href="/courses" className="underline underline-offset-2">
                Browse courses
              </Link>
              .
            </p>
          ) : (
            <ul className="space-y-2">
              {subscriptions.map((subscription) => (
                <li key={subscription.id} className="flex items-center justify-between gap-4 rounded-lg border px-3 py-2">
                  <div>
                    <Link
                      href={`/courses/${subscription.courseId}`}
                      className="text-sm font-medium hover:underline"
                    >
                      {subscription.course?.title}
                    </Link>
                    <p className="text-muted-foreground text-xs">
                      {subscription.startDate} → {subscription.endDate}
                    </p>
                  </div>
                  {subscription.status && (
                    <Badge variant={STATUS_VARIANT[subscription.status]}>{subscription.status}</Badge>
                  )}
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
