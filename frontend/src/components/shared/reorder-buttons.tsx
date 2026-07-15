"use client";

import { ChevronDownIcon, ChevronUpIcon } from "lucide-react";
import { Button } from "@/components/ui/button";

/** Ref: SRS 7.9, 7.10 - manual up/down ordering (an alternative the SRS explicitly allows to drag-and-drop). */
export function ReorderButtons({
  onMoveUp,
  onMoveDown,
  disableUp,
  disableDown,
  disabled,
}: {
  onMoveUp: () => void;
  onMoveDown: () => void;
  disableUp?: boolean;
  disableDown?: boolean;
  disabled?: boolean;
}) {
  return (
    <div className="flex flex-col">
      <Button
        type="button"
        variant="ghost"
        size="icon-xs"
        disabled={disabled || disableUp}
        onClick={onMoveUp}
        aria-label="Move up"
      >
        <ChevronUpIcon />
      </Button>
      <Button
        type="button"
        variant="ghost"
        size="icon-xs"
        disabled={disabled || disableDown}
        onClick={onMoveDown}
        aria-label="Move down"
      >
        <ChevronDownIcon />
      </Button>
    </div>
  );
}
