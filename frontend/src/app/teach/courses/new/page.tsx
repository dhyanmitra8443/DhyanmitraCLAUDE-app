import { CourseForm } from "@/components/courses/course-form";
import { listCategories } from "@/lib/categories/queries";
import { getSession } from "@/lib/auth/session";

export const metadata = { title: "Create course | Dhyan Mitra" };

export default async function NewInstructorCoursePage() {
  const [categoriesResult, session] = await Promise.all([listCategories({ size: 100 }), getSession()]);

  return (
    <div className="max-w-2xl space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Create course</h1>
      <CourseForm
        mode="create"
        role="instructor"
        categories={categoriesResult.content}
        fixedInstructorIds={session ? [session.userId] : []}
      />
    </div>
  );
}
