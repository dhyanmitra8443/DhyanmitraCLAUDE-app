import { CourseForm } from "@/components/courses/course-form";
import { listCategories } from "@/lib/categories/queries";
import { searchUsers } from "@/lib/users/queries";

export const metadata = { title: "Create course | Dhyan Mitra" };

export default async function NewAdminCoursePage() {
  const [categoriesResult, instructorsResult] = await Promise.all([
    listCategories({ size: 100 }),
    searchUsers({ role: "INSTRUCTOR", size: 100 }),
  ]);

  return (
    <div className="max-w-2xl space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Create course</h1>
      <CourseForm
        mode="create"
        role="admin"
        categories={categoriesResult.content}
        instructors={instructorsResult.content}
      />
    </div>
  );
}
