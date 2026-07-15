import { notFound } from "next/navigation";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { LessonForm } from "@/components/lessons/lesson-form";
import { LessonStatusActions } from "@/components/lessons/lesson-status-actions";
import { LessonPreviewAction } from "@/components/lessons/lesson-preview-action";
import { LessonResourcesManager } from "@/components/resources/lesson-resources-manager";
import { getLessonDetail } from "@/lib/lessons/queries";
import { listLessonResources } from "@/lib/resources/queries";
import { ApiError } from "@/lib/api/errors";

export const metadata = { title: "Edit lesson | Dhyan Mitra" };

const STATUS_VARIANT = {
  DRAFT: "secondary",
  PUBLISHED: "default",
  ARCHIVED: "outline",
} as const;

export default async function InstructorLessonDetailPage({
  params,
}: {
  params: Promise<{ id: string; sectionId: string; lessonId: string }>;
}) {
  const { sectionId, lessonId } = await params;

  let lesson;
  try {
    lesson = await getLessonDetail(lessonId);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) notFound();
    throw error;
  }

  const resources = await listLessonResources(lessonId);

  return (
    <div className="max-w-2xl space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">{lesson.title}</h1>
          <div className="mt-1 flex items-center gap-2">
            {lesson.isPreview && <Badge variant="outline">Preview</Badge>}
            {lesson.status && <Badge variant={STATUS_VARIANT[lesson.status]}>{lesson.status}</Badge>}
          </div>
        </div>
        <div className="flex flex-col items-end gap-2">
          {lesson.status && <LessonStatusActions lessonId={lessonId} status={lesson.status} />}
          <LessonPreviewAction lessonId={lessonId} isPreview={lesson.isPreview ?? false} />
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Details</CardTitle>
        </CardHeader>
        <CardContent>
          <LessonForm mode="edit" sectionId={sectionId} lesson={lesson} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Resources</CardTitle>
        </CardHeader>
        <CardContent>
          <LessonResourcesManager lessonId={lessonId} resources={resources} />
        </CardContent>
      </Card>
    </div>
  );
}
