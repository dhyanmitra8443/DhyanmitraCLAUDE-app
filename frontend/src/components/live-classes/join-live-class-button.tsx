"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { joinLiveClass } from "@/lib/live-classes/client";

/** Ref: SRS 11.8-11.10 - records attendance, then hands the student the meeting URL/password. */
export function JoinLiveClassButton({ liveClassId }: { liveClassId: string }) {
  const [isJoining, setIsJoining] = useState(false);

  async function onJoin() {
    setIsJoining(true);
    try {
      const { meetingUrl, meetingPassword } = await joinLiveClass(liveClassId);
      if (meetingPassword) {
        toast.info(`Meeting password: ${meetingPassword}`);
      }
      window.open(meetingUrl, "_blank", "noopener,noreferrer");
    } catch (error) {
      toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
    } finally {
      setIsJoining(false);
    }
  }

  return (
    <Button size="sm" disabled={isJoining} onClick={onJoin}>
      {isJoining ? "Joining…" : "Join"}
    </Button>
  );
}
