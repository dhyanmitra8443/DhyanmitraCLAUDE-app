import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { JoinLiveClassButton } from "@/components/live-classes/join-live-class-button";
import { getMySubscriptions } from "@/lib/subscriptions/queries";
import { listLiveClassesByCourse } from "@/lib/live-classes/queries";
import type { LiveClassSummary } from "@/lib/api/types";

export const metadata = { title: "Live classes | Dhyan Mitra" };

const STATUS_VARIANT = {
  SCHEDULED: "default",
  CANCELLED: "destructive",
  COMPLETED: "secondary",
} as const;

/** Ref: SRS 11.8-11.10 - live classes across every course the student has an active subscription to. */
export default async function StudentLiveClassesPage() {
  const subscriptions = (await getMySubscriptions()).filter((s) => s.status === "ACTIVE");

  const perCourse = await Promise.all(
    subscriptions
      .filter((s) => s.courseId)
      .map(async (s) => ({
        course: s.course,
        liveClasses: (await listLiveClassesByCourse(s.courseId!, { size: 50 }).catch(() => null))?.content ?? [],
      })),
  );

  const rows = perCourse
    .flatMap(({ course, liveClasses }) => liveClasses.map((liveClass) => ({ course, liveClass })))
    .sort((a, b) => (a.liveClass.scheduledDate ?? "").localeCompare(b.liveClass.scheduledDate ?? ""));

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Live classes</h1>

      <Card>
        <CardHeader>
          <CardTitle>Your live classes</CardTitle>
        </CardHeader>
        <CardContent>
          {rows.length === 0 ? (
            <p className="text-muted-foreground text-sm">
              No live classes scheduled.{" "}
              <Link href="/courses" className="underline underline-offset-2">
                Browse courses
              </Link>
              .
            </p>
          ) : (
            <ul className="space-y-2">
              {rows.map(({ course, liveClass }: { course?: { title?: string }; liveClass: LiveClassSummary }) => (
                <li key={liveClass.id} className="flex items-center justify-between gap-4 rounded-lg border px-3 py-2">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-medium">{liveClass.title}</span>
                      {liveClass.status && <Badge variant={STATUS_VARIANT[liveClass.status]}>{liveClass.status}</Badge>}
                    </div>
                    <p className="text-muted-foreground text-xs">
                      {course?.title} · {liveClass.scheduledDate} · {liveClass.scheduledTime}
                    </p>
                  </div>
                  {liveClass.status === "SCHEDULED" && liveClass.id && <JoinLiveClassButton liveClassId={liveClass.id} />}
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
