import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress, ProgressTrack, ProgressIndicator } from "@/components/ui/progress";
import type { CourseProgressSummary } from "@/lib/api/types";

/** Ref: SRS 12.6 - the signed-in student's own progress in this course. */
export function CourseProgressCard({ progress }: { progress: CourseProgressSummary }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Your progress</CardTitle>
      </CardHeader>
      <CardContent className="space-y-2">
        <div className="flex items-center justify-between gap-4 text-sm">
          <span className="text-muted-foreground">
            {progress.completedLessons ?? 0} of {progress.totalPublishedLessons ?? 0} lessons complete
          </span>
          <span className="font-medium tabular-nums">{Math.round(progress.progressPercentage ?? 0)}%</span>
        </div>
        <Progress value={progress.progressPercentage ?? 0}>
          <ProgressTrack>
            <ProgressIndicator />
          </ProgressTrack>
        </Progress>
        {progress.completionStatus === "COMPLETED" && (
          <p className="text-muted-foreground text-xs">
            Completed{progress.courseCompletedAt ? ` on ${new Date(progress.courseCompletedAt).toLocaleDateString()}` : ""}.
          </p>
        )}
      </CardContent>
    </Card>
  );
}
