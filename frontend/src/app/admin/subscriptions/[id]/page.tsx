import Link from "next/link";
import { notFound } from "next/navigation";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { SubscriptionCorrectionForm } from "@/components/subscriptions/subscription-correction-form";
import { getSubscriptionDetail } from "@/lib/subscriptions/queries";
import { getUserFullProfile } from "@/lib/users/queries";
import { ApiError } from "@/lib/api/errors";

export const metadata = { title: "Subscription | Dhyan Mitra" };

const STATUS_VARIANT = {
  ACTIVE: "default",
  EXPIRED: "secondary",
  CANCELLED: "destructive",
  PENDING: "outline",
} as const;

export default async function AdminSubscriptionDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  let subscription;
  try {
    subscription = await getSubscriptionDetail(id);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) notFound();
    throw error;
  }

  const student = subscription.studentId ? await getUserFullProfile(subscription.studentId).catch(() => null) : null;

  return (
    <div className="max-w-2xl space-y-6">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">{subscription.course?.title}</h1>
        <div className="mt-1 flex items-center gap-2">
          {subscription.status && <Badge variant={STATUS_VARIANT[subscription.status]}>{subscription.status}</Badge>}
          <span className="text-muted-foreground text-sm">
            {student ? `${student.firstName} ${student.lastName} (${student.email})` : subscription.studentId}
          </span>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Details</CardTitle>
        </CardHeader>
        <CardContent className="space-y-1 text-sm">
          <p>
            <span className="text-muted-foreground">Start date: </span>
            {subscription.startDate}
          </p>
          <p>
            <span className="text-muted-foreground">End date: </span>
            {subscription.endDate}
          </p>
          <p>
            <span className="text-muted-foreground">Purchase date: </span>
            {subscription.purchaseDate ? new Date(subscription.purchaseDate).toLocaleString() : "—"}
          </p>
          {subscription.courseId && (
            <p>
              <Link href={`/admin/courses/${subscription.courseId}`} className="underline underline-offset-2">
                View course
              </Link>
            </p>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Correct subscription</CardTitle>
        </CardHeader>
        <CardContent>
          <SubscriptionCorrectionForm subscription={subscription} />
        </CardContent>
      </Card>
    </div>
  );
}
