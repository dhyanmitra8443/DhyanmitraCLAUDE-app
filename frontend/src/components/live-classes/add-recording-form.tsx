"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ApiError } from "@/lib/api/errors";
import { addRecordingUrl } from "@/lib/live-classes/client";

/** Ref: SRS 11.11 - optional, must be private/unlisted domain-restricted hosting (same rule as lesson videos). */
export function AddRecordingForm({ liveClassId, recordingUrl }: { liveClassId: string; recordingUrl?: string | null }) {
  const router = useRouter();
  const [value, setValue] = useState(recordingUrl ?? "");
  const [isSubmitting, setIsSubmitting] = useState(false);

  return (
    <form
      onSubmit={async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        try {
          await addRecordingUrl(liveClassId, value);
          toast.success("Recording URL added.");
          router.refresh();
        } catch (error) {
          toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
        } finally {
          setIsSubmitting(false);
        }
      }}
      className="flex items-center gap-2"
    >
      <Input
        value={value}
        onChange={(e) => setValue(e.target.value)}
        placeholder="Recording URL"
        className="max-w-sm"
      />
      <Button type="submit" size="sm" variant="outline" disabled={isSubmitting || !value}>
        {isSubmitting ? "Saving…" : "Save recording"}
      </Button>
    </form>
  );
}
