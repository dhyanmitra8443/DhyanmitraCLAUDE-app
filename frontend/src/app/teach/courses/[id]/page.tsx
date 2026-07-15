import { notFound } from "next/navigation";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { CourseForm } from "@/components/courses/course-form";
import { CourseCategoriesManager } from "@/components/courses/course-categories-manager";
import { CourseStatusActions } from "@/components/courses/course-status-actions";
import { CourseContentManager } from "@/components/sections/course-content-manager";
import { SubscriptionPlansReadOnly } from "@/components/subscriptions/subscription-plans-readonly";
import { LiveClassesManager } from "@/components/live-classes/live-classes-manager";
import { CourseProgressTable } from "@/components/progress/course-progress-table";
import { getCourseDetail } from "@/lib/courses/queries";
import { listCategories } from "@/lib/categories/queries";
import { getCourseOutline } from "@/lib/sections/queries";
import { listSubscriptionPlans } from "@/lib/subscriptions/queries";
import { listLiveClassesByCourse } from "@/lib/live-classes/queries";
import { listCourseProgress } from "@/lib/progress/queries";
import { ApiError } from "@/lib/api/errors";

export const metadata = { title: "Edit course | Dhyan Mitra" };

const STATUS_VARIANT = {
  DRAFT: "secondary",
  PUBLISHED: "default",
  ARCHIVED: "outline",
} as const;

export default async function InstructorCourseDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  let course;
  try {
    course = await getCourseDetail(id);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) notFound();
    throw error;
  }

  const [categoriesResult, sections, plans, liveClasses, progress] = await Promise.all([
    listCategories({ size: 100 }),
    getCourseOutline(id),
    listSubscriptionPlans(id),
    listLiveClassesByCourse(id, { size: 50 }),
    listCourseProgress(id, { size: 50 }),
  ]);

  return (
    <div className="max-w-2xl space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">{course.title}</h1>
          {course.status && <Badge variant={STATUS_VARIANT[course.status]}>{course.status}</Badge>}
        </div>
        {course.status && <CourseStatusActions courseId={id} status={course.status} />}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Details</CardTitle>
        </CardHeader>
        <CardContent>
          <CourseForm mode="edit" role="instructor" course={course} categories={categoriesResult.content} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Categories</CardTitle>
        </CardHeader>
        <CardContent>
          <CourseCategoriesManager
            courseId={id}
            current={course.categories ?? []}
            available={categoriesResult.content}
          />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Content</CardTitle>
        </CardHeader>
        <CardContent>
          <CourseContentManager courseId={id} sections={sections} basePath={`/teach/courses/${id}`} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Subscription plans</CardTitle>
        </CardHeader>
        <CardContent>
          <SubscriptionPlansReadOnly plans={plans} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Live classes</CardTitle>
        </CardHeader>
        <CardContent>
          <LiveClassesManager courseId={id} liveClasses={liveClasses.content} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Student progress</CardTitle>
        </CardHeader>
        <CardContent>
          <CourseProgressTable rows={progress.content} />
        </CardContent>
      </Card>
    </div>
  );
}
