import { SectionForm } from "@/components/sections/section-form";

export const metadata = { title: "Add section | Dhyan Mitra" };

export default async function NewAdminSectionPage({ params }: { params: Promise<{ id: string }> }) {
  const { id: courseId } = await params;

  return (
    <div className="max-w-md space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Add section</h1>
      <SectionForm mode="create" courseId={courseId} basePath={`/admin/courses/${courseId}`} />
    </div>
  );
}
