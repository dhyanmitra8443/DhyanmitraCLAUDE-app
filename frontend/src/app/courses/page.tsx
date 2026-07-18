import { CourseCard } from "@/components/courses/course-card";
import { CourseFilterBar } from "@/components/courses/course-filter-bar";
import { PaginationControls } from "@/components/shared/pagination-controls";
import { listCourses } from "@/lib/courses/queries";
import { listCategories } from "@/lib/categories/queries";
import { PublicShell } from "@/components/marketing/public-shell";

export const metadata = { title: "Courses | Dhyan Mitra" };

export default async function CourseCatalogPage({
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
  const categoryId = params.categoryId
    ? Array.isArray(params.categoryId)
      ? params.categoryId
      : [params.categoryId]
    : undefined;

  const [result, categoriesResult] = await Promise.all([
    // Always PUBLISHED regardless of caller: the backend only auto-forces this
    // for non-admins, and this page is the public-facing catalog no matter
    // who's viewing it (an admin/instructor browsing it should see exactly
    // what a visitor sees - /admin/courses is the place to see drafts).
    listCourses({
      page,
      size: 12,
      sort: "publishedAt,desc",
      search,
      difficultyLevel,
      language,
      categoryId,
      status: "PUBLISHED",
    }),
    listCategories({ size: 50 }),
  ]);

  return (
    <PublicShell>
      <div className="mx-auto max-w-6xl space-y-6 px-6 py-10">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">Courses</h1>
          <p className="text-muted-foreground mt-2 text-sm">
            Browse structured yoga programmes for every level.
          </p>
        </div>

        <CourseFilterBar categories={categoriesResult.content} basePath="/courses" />

        {result.content.length === 0 ? (
          <p className="text-muted-foreground text-sm">No courses match these filters.</p>
        ) : (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {result.content.map((course) => (
              <CourseCard key={course.id} course={course} />
            ))}
          </div>
        )}

        <PaginationControls page={result.page.page ?? 0} totalPages={result.page.totalPages ?? 1} />
      </div>
    </PublicShell>
  );
}
