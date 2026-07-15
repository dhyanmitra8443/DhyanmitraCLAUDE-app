import Link from "next/link";
import { notFound } from "next/navigation";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { LiveClassForm } from "@/components/live-classes/live-class-form";
import { LiveClassCancelAction } from "@/components/live-classes/live-class-cancel-action";
import { AddRecordingForm } from "@/components/live-classes/add-recording-form";
import { getLiveClassDetail } from "@/lib/live-classes/queries";
import { ApiError } from "@/lib/api/errors";

export const metadata = { title: "Live class | Dhyan Mitra" };

const STATUS_VARIANT = {
  SCHEDULED: "default",
  CANCELLED: "destructive",
  COMPLETED: "secondary",
} as const;

export default async function AdminLiveClassDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  let liveClass;
  try {
    liveClass = await getLiveClassDetail(id);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) notFound();
    throw error;
  }

  return (
    <div className="max-w-2xl space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">{liveClass.title}</h1>
          <div className="mt-1 flex items-center gap-2">
            {liveClass.status && <Badge variant={STATUS_VARIANT[liveClass.status]}>{liveClass.status}</Badge>}
            {liveClass.courseId && (
              <Link href={`/admin/courses/${liveClass.courseId}`} className="text-muted-foreground text-sm underline underline-offset-2">
                View course
              </Link>
            )}
          </div>
        </div>
        {liveClass.status === "SCHEDULED" && liveClass.id && <LiveClassCancelAction liveClassId={liveClass.id} />}
      </div>

      {liveClass.status !== "CANCELLED" && liveClass.courseId && (
        <Card>
          <CardHeader>
            <CardTitle>Details</CardTitle>
          </CardHeader>
          <CardContent>
            <LiveClassForm courseId={liveClass.courseId} mode="edit" liveClass={liveClass} />
          </CardContent>
        </Card>
      )}

      {liveClass.id && (
        <Card>
          <CardHeader>
            <CardTitle>Recording</CardTitle>
          </CardHeader>
          <CardContent>
            <AddRecordingForm liveClassId={liveClass.id} recordingUrl={liveClass.recordingUrl} />
          </CardContent>
        </Card>
      )}
    </div>
  );
}
