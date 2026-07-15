import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import type { SectionDetail } from "@/lib/api/types";

/**
 * Ref: SRS 7.12, 7.14 - the outline endpoint doesn't compute a per-lesson
 * lock flag (Ref research: no subscription cross-check happens there), so
 * only the preview lesson gets a real link here; everything else shows a
 * locked state rather than linking through to a 403.
 */
export function PublicCourseOutline({ courseId, sections }: { courseId: string; sections: SectionDetail[] }) {
  if (sections.length === 0) {
    return <p className="text-muted-foreground text-sm">Course content coming soon.</p>;
  }

  return (
    <div className="space-y-4">
      {sections.map((section) => (
        <div key={section.id}>
          <h3 className="mb-2 font-medium">{section.title}</h3>
          <ul className="space-y-1">
            {(section.lessons ?? []).map((lesson) =>
              lesson.isPreview ? (
                <li key={lesson.id}>
                  <Link
                    href={`/courses/${courseId}/lessons/${lesson.id}`}
                    className="flex items-center justify-between rounded-lg border px-3 py-2 text-sm hover:bg-muted"
                  >
                    <span>{lesson.title}</span>
                    <Badge>Preview</Badge>
                  </Link>
                </li>
              ) : (
                <li
                  key={lesson.id}
                  className="text-muted-foreground flex items-center justify-between rounded-lg border px-3 py-2 text-sm"
                >
                  <span>{lesson.title}</span>
                  <span className="text-xs">Subscription required</span>
                </li>
              ),
            )}
          </ul>
        </div>
      ))}
    </div>
  );
}
