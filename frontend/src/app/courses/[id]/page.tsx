import Link from "next/link";
import { notFound } from "next/navigation";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { PublicCourseOutline } from "@/components/sections/public-course-outline";
import { CheckoutButton } from "@/components/payments/checkout-button";
import { JoinLiveClassButton } from "@/components/live-classes/join-live-class-button";
import { CourseProgressCard } from "@/components/progress/course-progress-card";
import { CourseThumbnail } from "@/components/courses/course-thumbnail";
import { getCourseDetail } from "@/lib/courses/queries";
import { getCourseOutline } from "@/lib/sections/queries";
import { listSubscriptionPlans, getMySubscriptions } from "@/lib/subscriptions/queries";
import { listLiveClassesByCourse } from "@/lib/live-classes/queries";
import { getOwnCourseProgress } from "@/lib/progress/queries";
import { getSession } from "@/lib/auth/session";
import { formatCurrency } from "@/lib/format";
import { ApiError } from "@/lib/api/errors";

export const metadata = { title: "Course | Dhyan Mitra" };

export default async function CourseDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  let course;
  try {
    course = await getCourseDetail(id);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) notFound();
    throw error;
  }

  const session = await getSession();
  const [sections, plans, liveClassesResult, progress, subscriptions] = await Promise.all([
    getCourseOutline(id),
    listSubscriptionPlans(id),
    // Ref: SRS 11.8 - the endpoint requires auth (admin/instructor always,
    // students only with an active subscription); skip it entirely for
    // anonymous visitors and swallow the 403 for signed-in-but-unsubscribed
    // students rather than failing the whole page.
    session ? listLiveClassesByCourse(id, { size: 20 }).catch(() => null) : Promise.resolve(null),
    // Ref: SRS 12.6 - student-only; swallow the 403 for a signed-in
    // non-student (admin/instructor previewing) rather than failing the page.
    session?.role === "STUDENT" ? getOwnCourseProgress(id).catch(() => null) : Promise.resolve(null),
    // Used only to hide the purchase CTA for a student who's already enrolled.
    session?.role === "STUDENT" ? getMySubscriptions().catch(() => []) : Promise.resolve([]),
  ]);
  const liveClasses = liveClassesResult?.content ?? [];
  const hasActiveSubscription = subscriptions.some((s) => s.courseId === id && s.status === "ACTIVE");
  const isAssignedStaff =
    session &&
    (session.role === "ADMINISTRATOR" ||
      (session.role === "INSTRUCTOR" && course.instructors?.some((i) => i.id === session.userId)));

  return (
    <div className="mx-auto max-w-4xl space-y-6 px-6 py-10">
      <Link href="/courses" className="text-muted-foreground text-sm hover:underline">
        ← Back to courses
      </Link>

      <div className="bg-muted aspect-video w-full overflow-hidden rounded-xl">
        <CourseThumbnail src={course.thumbnailUrl} className="h-full w-full object-cover" />
      </div>

      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">{course.title}</h1>
          <p className="text-muted-foreground mt-1">{course.shortDescription}</p>
        </div>
        {isAssignedStaff && (
          <Link
            href={session!.role === "ADMINISTRATOR" ? `/admin/courses/${id}` : `/teach/courses/${id}`}
            className={cn(buttonVariants({ variant: "outline", size: "sm" }))}
          >
            Manage course
          </Link>
        )}
      </div>

      <div className="flex flex-wrap items-center gap-2">
        {course.difficultyLevel && <Badge variant="outline">{course.difficultyLevel}</Badge>}
        {course.language && <Badge variant="outline">{course.language}</Badge>}
        <span className="text-muted-foreground text-sm">{course.lessonCount ?? 0} lessons</span>
        {course.estimatedDurationMinutes != null && (
          <span className="text-muted-foreground text-sm">{course.estimatedDurationMinutes} min</span>
        )}
        {course.categories?.map((category) => <Badge key={category.id}>{category.name}</Badge>)}
      </div>

      {progress && <CourseProgressCard progress={progress} />}

      <Card>
        <CardHeader>
          <CardTitle>About this course</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="whitespace-pre-line text-sm">{course.detailedDescription}</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Course content</CardTitle>
        </CardHeader>
        <CardContent>
          <PublicCourseOutline courseId={id} sections={sections} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Instructors</CardTitle>
        </CardHeader>
        <CardContent>
          {(course.instructors ?? []).length === 0 ? (
            <p className="text-muted-foreground text-sm">No instructors assigned yet.</p>
          ) : (
            <ul className="space-y-1 text-sm">
              {course.instructors!.map((instructor) => (
                <li key={instructor.id}>
                  {instructor.firstName} {instructor.lastName}
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>

      {liveClasses.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Live classes</CardTitle>
          </CardHeader>
          <CardContent>
            <ul className="space-y-2">
              {liveClasses.map((liveClass) => (
                <li key={liveClass.id} className="flex items-center justify-between gap-4 rounded-lg border px-3 py-2">
                  <div>
                    <p className="text-sm font-medium">{liveClass.title}</p>
                    <p className="text-muted-foreground text-xs">
                      {liveClass.scheduledDate} · {liveClass.scheduledTime}
                    </p>
                  </div>
                  {liveClass.status === "SCHEDULED" && session?.role === "STUDENT" && liveClass.id && (
                    <JoinLiveClassButton liveClassId={liveClass.id} />
                  )}
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Enroll</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {hasActiveSubscription ? (
            <p className="text-muted-foreground text-sm">You&apos;re enrolled in this course.</p>
          ) : (
            <>
              {plans.length === 0 ? (
                <p className="text-muted-foreground text-sm">No subscription plans available yet.</p>
              ) : (
                <ul className="space-y-2">
                  {plans.map((plan) => (
                    <li key={plan.id} className="flex items-center justify-between gap-4 rounded-lg border px-3 py-2">
                      <div>
                        <p className="text-sm font-medium">{plan.planName}</p>
                        <p className="text-muted-foreground text-xs">
                          {plan.duration} {plan.durationUnit?.toLowerCase()}
                          {(plan.duration ?? 0) > 1 ? "s" : ""} access
                        </p>
                      </div>
                      <div className="flex items-center gap-3">
                        <span className="text-sm font-semibold">
                          {plan.price != null ? formatCurrency(plan.price) : ""}
                        </span>
                        {session?.role === "STUDENT" && plan.id && (
                          <CheckoutButton courseId={id} subscriptionPlanId={plan.id} email={session.email} />
                        )}
                      </div>
                    </li>
                  ))}
                </ul>
              )}

              {!session ? (
                <Link href={`/sign-in?next=/courses/${id}`} className={cn(buttonVariants())}>
                  Sign in to enroll
                </Link>
              ) : session.role !== "STUDENT" ? (
                <p className="text-muted-foreground text-sm">Enrollment is only available to students.</p>
              ) : null}
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
