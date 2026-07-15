import { notFound } from "next/navigation";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { SectionForm } from "@/components/sections/section-form";
import { SectionStatusActions } from "@/components/sections/section-status-actions";
import { LessonListManager } from "@/components/lessons/lesson-list-manager";
import { getCourseOutline } from "@/lib/sections/queries";

export const metadata = { title: "Edit section | Dhyan Mitra" };

const STATUS_VARIANT = {
  DRAFT: "secondary",
  PUBLISHED: "default",
  ARCHIVED: "outline",
} as const;

export default async function AdminSectionDetailPage({
  params,
}: {
  params: Promise<{ id: string; sectionId: string }>;
}) {
  const { id: courseId, sectionId } = await params;

  // There's no standalone GET /sections/{id} - the course outline is the
  // only endpoint that returns a section with its nested lessons.
  const sections = await getCourseOutline(courseId);
  const section = sections.find((s) => s.id === sectionId);
  if (!section) notFound();

  const basePath = `/admin/courses/${courseId}/sections/${sectionId}`;

  return (
    <div className="max-w-2xl space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">{section.title}</h1>
          {section.status && <Badge variant={STATUS_VARIANT[section.status]}>{section.status}</Badge>}
        </div>
        {section.status && <SectionStatusActions sectionId={sectionId} status={section.status} />}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Details</CardTitle>
        </CardHeader>
        <CardContent>
          <SectionForm mode="edit" courseId={courseId} section={section} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Lessons</CardTitle>
        </CardHeader>
        <CardContent>
          <LessonListManager sectionId={sectionId} lessons={section.lessons ?? []} basePath={basePath} />
        </CardContent>
      </Card>
    </div>
  );
}
