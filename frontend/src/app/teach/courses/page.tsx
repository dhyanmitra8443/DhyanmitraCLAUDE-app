import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { getInstructorDashboard } from "@/lib/dashboard/queries";
import { formatCompactNumber } from "@/lib/format";

export const metadata = { title: "Courses | Dhyan Mitra" };

const STATUS_VARIANT = {
  DRAFT: "secondary",
  PUBLISHED: "default",
  ARCHIVED: "outline",
} as const;

/**
 * The public /courses listing forces every non-admin caller to PUBLISHED
 * only (Ref: SRS 5.9), so it can never show an instructor their own drafts.
 * The instructor dashboard's courseSummaries is the one endpoint that
 * reports an instructor's own courses regardless of status - use that here
 * instead of the catalog endpoint.
 */
export default async function InstructorCoursesPage() {
  const dashboard = await getInstructorDashboard();
  const courses = dashboard.courseSummaries ?? [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold tracking-tight">Courses</h1>
        <Link href="/teach/courses/new" className={cn(buttonVariants())}>
          Create course
        </Link>
      </div>

      {courses.length === 0 ? (
        <p className="text-muted-foreground text-sm">No courses assigned yet.</p>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {courses.map((course) => (
            <Link key={course.courseId} href={`/teach/courses/${course.courseId}`}>
              <Card className="h-full transition-shadow hover:shadow-md">
                <CardContent className="space-y-2">
                  <div className="flex items-start justify-between gap-2">
                    <h3 className="font-medium">{course.courseName}</h3>
                    {course.courseStatus && (
                      <Badge variant={STATUS_VARIANT[course.courseStatus]}>{course.courseStatus}</Badge>
                    )}
                  </div>
                  <p className="text-muted-foreground text-sm">
                    {formatCompactNumber(course.activeStudents ?? 0)} active /{" "}
                    {formatCompactNumber(course.totalStudents ?? 0)} total students
                  </p>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
