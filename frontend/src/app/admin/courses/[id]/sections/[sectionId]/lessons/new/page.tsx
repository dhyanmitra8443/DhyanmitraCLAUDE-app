import { LessonForm } from "@/components/lessons/lesson-form";

export const metadata = { title: "Add lesson | Dhyan Mitra" };

export default async function NewAdminLessonPage({
  params,
}: {
  params: Promise<{ id: string; sectionId: string }>;
}) {
  const { id: courseId, sectionId } = await params;

  return (
    <div className="max-w-2xl space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Add lesson</h1>
      <LessonForm
        mode="create"
        sectionId={sectionId}
        basePath={`/admin/courses/${courseId}/sections/${sectionId}`}
      />
    </div>
  );
}
