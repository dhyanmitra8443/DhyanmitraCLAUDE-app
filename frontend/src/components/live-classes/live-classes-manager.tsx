"use client";

import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { LiveClassForm } from "./live-class-form";
import { LiveClassCancelAction } from "./live-class-cancel-action";
import { AddRecordingForm } from "./add-recording-form";
import type { LiveClassSummary } from "@/lib/api/types";

const STATUS_VARIANT = {
  SCHEDULED: "default",
  CANCELLED: "destructive",
  COMPLETED: "secondary",
} as const;

/** Ref: SRS 11.6, 11.7, 11.12, 11.13 - administrator or assigned instructor management of a course's live classes. */
export function LiveClassesManager({ courseId, liveClasses }: { courseId: string; liveClasses: LiveClassSummary[] }) {
  const [editingId, setEditingId] = useState<string | null>(null);
  const [recordingForId, setRecordingForId] = useState<string | null>(null);

  return (
    <div className="space-y-3">
      {liveClasses.length === 0 ? (
        <p className="text-muted-foreground text-sm">No live classes scheduled yet.</p>
      ) : (
        <ul className="space-y-2">
          {liveClasses.map((liveClass) =>
            liveClass.id === editingId ? (
              <li key={liveClass.id}>
                <LiveClassForm
                  courseId={courseId}
                  mode="edit"
                  liveClass={liveClass}
                  onDone={() => setEditingId(null)}
                />
              </li>
            ) : (
              <li key={liveClass.id} className="space-y-2 rounded-lg border px-3 py-2">
                <div className="flex items-center gap-2">
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-medium">{liveClass.title}</span>
                      {liveClass.status && <Badge variant={STATUS_VARIANT[liveClass.status]}>{liveClass.status}</Badge>}
                    </div>
                    <p className="text-muted-foreground text-xs">
                      {liveClass.scheduledDate} · {liveClass.scheduledTime}
                    </p>
                  </div>
                  {liveClass.status !== "CANCELLED" && (
                    <Button size="sm" variant="outline" onClick={() => setEditingId(liveClass.id ?? null)}>
                      Edit
                    </Button>
                  )}
                  {liveClass.status === "SCHEDULED" && liveClass.id && (
                    <LiveClassCancelAction liveClassId={liveClass.id} />
                  )}
                  {liveClass.id && (
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => setRecordingForId(recordingForId === liveClass.id ? null : liveClass.id!)}
                    >
                      {liveClass.recordingUrl ? "Edit recording" : "Add recording"}
                    </Button>
                  )}
                </div>
                {recordingForId === liveClass.id && liveClass.id && (
                  <AddRecordingForm liveClassId={liveClass.id} recordingUrl={liveClass.recordingUrl} />
                )}
              </li>
            ),
          )}
        </ul>
      )}

      <LiveClassForm courseId={courseId} mode="create" />
    </div>
  );
}
