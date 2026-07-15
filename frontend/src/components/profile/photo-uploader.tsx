"use client";

import { useRef, useState, useTransition } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { uploadOwnPhoto } from "@/lib/users/client";
import { resolveUploadUrl } from "@/lib/uploads";

const MAX_SIZE_BYTES = 5 * 1024 * 1024;
const ACCEPTED_TYPES = ["image/jpeg", "image/png"];

/** Ref: SRS 4.13 - JPG/PNG only, max 5 MB. Client-side check is UX only; the backend re-validates. */
export function PhotoUploader({
  firstName,
  lastName,
  photoUrl,
}: {
  firstName: string;
  lastName: string;
  photoUrl: string | null | undefined;
}) {
  const router = useRouter();
  const inputRef = useRef<HTMLInputElement>(null);
  const [isPending, startTransition] = useTransition();
  const [preview, setPreview] = useState<string | null>(null);
  const initials = `${firstName[0] ?? ""}${lastName[0] ?? ""}`.toUpperCase();

  function handleFile(file: File) {
    if (!ACCEPTED_TYPES.includes(file.type)) {
      toast.error("Only JPG and PNG images are allowed.");
      return;
    }
    if (file.size > MAX_SIZE_BYTES) {
      toast.error("Image must be 5 MB or smaller.");
      return;
    }

    setPreview(URL.createObjectURL(file));
    startTransition(async () => {
      try {
        await uploadOwnPhoto(file);
        toast.success("Profile photo updated.");
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
        setPreview(null);
      }
    });
  }

  return (
    <div className="flex items-center gap-4">
      <Avatar size="lg">
        <AvatarImage src={preview ?? resolveUploadUrl(photoUrl)} alt="" />
        <AvatarFallback>{initials || "?"}</AvatarFallback>
      </Avatar>
      <div>
        <input
          ref={inputRef}
          type="file"
          accept="image/jpeg,image/png"
          className="hidden"
          onChange={(e) => {
            const file = e.target.files?.[0];
            if (file) handleFile(file);
            e.target.value = "";
          }}
        />
        <Button type="button" variant="outline" size="sm" disabled={isPending} onClick={() => inputRef.current?.click()}>
          {isPending ? "Uploading…" : "Change photo"}
        </Button>
        <p className="text-muted-foreground mt-1 text-xs">JPG or PNG, up to 5 MB.</p>
      </div>
    </div>
  );
}
