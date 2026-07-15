import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ResourceDownloadButton } from "@/components/resources/resource-download-button";
import { getLessonDetail } from "@/lib/lessons/queries";
import { listLessonResources } from "@/lib/resources/queries";
import { toEmbedUrl } from "@/lib/video";
import { ApiError } from "@/lib/api/errors";

const FILE_RESOURCE_TYPES = new Set(["PDF", "IMAGE", "AUDIO", "ZIP"]);

export const metadata = { title: "Lesson | Dhyan Mitra" };

export default async function PublicLessonPage({
  params,
}: {
  params: Promise<{ id: string; lessonId: string }>;
}) {
  const { id: courseId, lessonId } = await params;

  let lesson;
  let accessDenied = false;
  try {
    lesson = await getLessonDetail(lessonId);
  } catch (error) {
    if (error instanceof ApiError && error.status === 403) {
      accessDenied = true;
    } else {
      throw error;
    }
  }

  // Ref: SRS 8.11 - same access rule as the lesson itself, so only fetch once access is confirmed.
  const resources = lesson ? await listLessonResources(lessonId) : [];
  const supportingResources = resources.filter((r) => r.resourceType !== "VIDEO" && r.status === "ACTIVE");

  return (
    <div className="mx-auto max-w-4xl space-y-6 px-6 py-10">
      <Link href={`/courses/${courseId}`} className="text-muted-foreground text-sm hover:underline">
        ← Back to course
      </Link>

      {accessDenied || !lesson ? (
        <Card>
          <CardContent className="space-y-2 py-6 text-center">
            <p className="font-medium">This lesson requires an active subscription.</p>
            <p className="text-muted-foreground text-sm">
              Subscribe to this course to unlock it.{" "}
              <Link href={`/courses/${courseId}`} className="underline underline-offset-2">
                View plans
              </Link>
              .
            </p>
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="aspect-video w-full overflow-hidden rounded-xl bg-black">
            <iframe
              src={toEmbedUrl(lesson.videoUrl ?? "")}
              className="h-full w-full"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowFullScreen
            />
          </div>

          <div>
            <h1 className="text-2xl font-semibold tracking-tight">{lesson.title}</h1>
            {lesson.shortDescription && <p className="text-muted-foreground mt-1">{lesson.shortDescription}</p>}
          </div>

          {lesson.detailedDescription && (
            <Card>
              <CardHeader>
                <CardTitle>About this lesson</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="whitespace-pre-line text-sm">{lesson.detailedDescription}</p>
              </CardContent>
            </Card>
          )}

          {supportingResources.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle>Resources</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  {supportingResources.map((resource) => (
                    <li key={resource.id} className="flex items-center justify-between gap-4 rounded-lg border px-3 py-2">
                      <span className="text-sm font-medium">{resource.displayName}</span>
                      {resource.resourceType === "EXTERNAL_LINK" && resource.externalUrl ? (
                        <a
                          href={resource.externalUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-sm underline underline-offset-2"
                        >
                          Open
                        </a>
                      ) : (
                        resource.id &&
                        FILE_RESOURCE_TYPES.has(resource.resourceType ?? "") && (
                          <ResourceDownloadButton resourceId={resource.id} />
                        )
                      )}
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          )}
        </>
      )}
    </div>
  );
}
