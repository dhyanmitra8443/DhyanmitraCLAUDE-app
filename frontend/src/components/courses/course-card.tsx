import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import type { CourseSummary } from "@/lib/api/types";

const STATUS_VARIANT = {
  DRAFT: "secondary",
  PUBLISHED: "default",
  ARCHIVED: "outline",
} as const;

export function CourseCard({
  course,
  href,
  showStatus = false,
}: {
  course: CourseSummary;
  href?: string;
  showStatus?: boolean;
}) {
  return (
    <Link href={href ?? `/courses/${course.id}`}>
      <Card className="h-full overflow-hidden transition-shadow hover:shadow-md">
        <div className="bg-muted aspect-video w-full overflow-hidden">
          {course.thumbnailUrl && (
            // eslint-disable-next-line @next/next/no-img-element -- arbitrary externally-hosted URLs, not a configured next/image domain
            <img src={course.thumbnailUrl} alt="" className="h-full w-full object-cover" />
          )}
        </div>
        <CardContent className="space-y-2">
          <h3 className="font-medium">{course.title}</h3>
          <p className="text-muted-foreground line-clamp-2 text-sm">{course.shortDescription}</p>
          <div className="flex flex-wrap items-center gap-1.5">
            {showStatus && course.status && (
              <Badge variant={STATUS_VARIANT[course.status]}>{course.status}</Badge>
            )}
            {course.difficultyLevel && <Badge variant="outline">{course.difficultyLevel}</Badge>}
            <span className="text-muted-foreground text-xs">{course.lessonCount ?? 0} lessons</span>
          </div>
        </CardContent>
      </Card>
    </Link>
  );
}
