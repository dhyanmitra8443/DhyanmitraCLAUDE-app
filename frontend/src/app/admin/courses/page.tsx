import Link from "next/link";
import { CourseCard } from "@/components/courses/course-card";
import { CourseFilterBar } from "@/components/courses/course-filter-bar";
import { PaginationControls } from "@/components/shared/pagination-controls";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { listCourses } from "@/lib/courses/queries";
import { listCategories } from "@/lib/categories/queries";

export const metadata = { title: "Courses | Dhyan Mitra" };

export default async function AdminCoursesPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const page = Number(params.page ?? 0);
  const search = typeof params.search === "string" ? params.search : undefined;
  const difficultyLevel =
    typeof params.difficultyLevel === "string"
      ? (params.difficultyLevel as "BEGINNER" | "INTERMEDIATE" | "ADVANCED")
      : undefined;
  const language = typeof params.language === "string" ? params.language : undefined;
  const status =
    typeof params.status === "string" ? (params.status as "DRAFT" | "PUBLISHED" | "ARCHIVED") : undefined;
  const categoryId = params.categoryId
    ? Array.isArray(params.categoryId)
      ? params.categoryId
      : [params.categoryId]
    : undefined;

  const [result, categoriesResult] = await Promise.all([
    listCourses({ page, size: 12, sort: "createdAt,desc", search, difficultyLevel, language, status, categoryId }),
    listCategories({ size: 50, status: "ACTIVE" }),
  ]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold tracking-tight">Courses</h1>
        <Link href="/admin/courses/new" className={cn(buttonVariants())}>
          Create course
        </Link>
      </div>

      <CourseFilterBar categories={categoriesResult.content} basePath="/admin/courses" showStatus />

      {result.content.length === 0 ? (
        <p className="text-muted-foreground text-sm">No courses match these filters.</p>
      ) : (
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {result.content.map((course) => (
            <CourseCard key={course.id} course={course} href={`/admin/courses/${course.id}`} showStatus />
          ))}
        </div>
      )}

      <PaginationControls page={result.page.page ?? 0} totalPages={result.page.totalPages ?? 1} />
    </div>
  );
}
