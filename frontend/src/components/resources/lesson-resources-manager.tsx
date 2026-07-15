"use client";

import { useState, useTransition } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { ApiError } from "@/lib/api/errors";
import { archiveLessonResource, createLessonResource, uploadResourceFile } from "@/lib/resources/client";
import { ResourceDownloadButton } from "./resource-download-button";
import type { LessonResourceSummary } from "@/lib/api/types";

const selectClassName =
  "h-8 rounded-lg border border-input bg-transparent px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30";

const FILE_RESOURCE_TYPES = ["PDF", "IMAGE", "AUDIO", "ZIP"] as const;
const SUPPORTING_RESOURCE_TYPES = [...FILE_RESOURCE_TYPES, "EXTERNAL_LINK"] as const;

const ACCEPT_BY_TYPE: Record<(typeof FILE_RESOURCE_TYPES)[number], string> = {
  PDF: "application/pdf",
  IMAGE: "image/*",
  AUDIO: "audio/*",
  ZIP: ".zip,application/zip",
};

/** Ref: SRS Chapter 8 - video (exactly one active) + supporting resources for a lesson. */
export function LessonResourcesManager({
  lessonId,
  resources,
}: {
  lessonId: string;
  resources: LessonResourceSummary[];
}) {
  const activeVideo = resources.find((r) => r.resourceType === "VIDEO" && r.status === "ACTIVE");
  const otherResources = resources.filter((r) => r.resourceType !== "VIDEO");

  return (
    <div className="space-y-6">
      <div>
        <h3 className="mb-2 text-sm font-medium">Video</h3>
        {activeVideo ? <ResourceRow resource={activeVideo} /> : <AddVideoForm lessonId={lessonId} />}
      </div>

      <div>
        <h3 className="mb-2 text-sm font-medium">Supporting resources</h3>
        {otherResources.length === 0 ? (
          <p className="text-muted-foreground mb-3 text-sm">No supporting resources yet.</p>
        ) : (
          <ul className="mb-3 space-y-2">
            {otherResources.map((resource) => (
              <li key={resource.id}>
                <ResourceRow resource={resource} />
              </li>
            ))}
          </ul>
        )}
        <AddResourceForm lessonId={lessonId} />
      </div>
    </div>
  );
}

function ResourceRow({ resource }: { resource: LessonResourceSummary }) {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();

  function handleArchive() {
    if (!resource.id) return;
    startTransition(async () => {
      try {
        await archiveLessonResource(resource.id!);
        toast.success("Resource archived.");
        router.refresh();
      } catch (error) {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
    });
  }

  const isFileType = (FILE_RESOURCE_TYPES as readonly string[]).includes(resource.resourceType ?? "");

  return (
    <div className="flex items-center gap-2 rounded-lg border px-3 py-2">
      <div className="flex-1">
        <div className="flex items-center gap-2">
          <span className="text-sm font-medium">{resource.displayName}</span>
          <Badge variant="outline">{resource.resourceType}</Badge>
          {resource.status && resource.status !== "ACTIVE" && <Badge variant="secondary">{resource.status}</Badge>}
        </div>
        {resource.resourceType === "EXTERNAL_LINK" && resource.externalUrl && (
          <a
            href={resource.externalUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="text-muted-foreground text-xs underline underline-offset-2"
          >
            {resource.externalUrl}
          </a>
        )}
        {resource.resourceType === "VIDEO" && resource.externalUrl && (
          <p className="text-muted-foreground truncate text-xs">{resource.externalUrl}</p>
        )}
      </div>

      {isFileType && resource.id && <ResourceDownloadButton resourceId={resource.id} />}
      {resource.status === "ACTIVE" && (
        <Button size="sm" variant="destructive" disabled={isPending} onClick={handleArchive}>
          {isPending ? "Archiving…" : "Archive"}
        </Button>
      )}
    </div>
  );
}

const videoSchema = z.object({
  displayName: z.string().min(1, "Name is required."),
  externalUrl: z.string().min(1, "Video URL is required."),
});

function AddVideoForm({ lessonId }: { lessonId: string }) {
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);
  const form = useForm<z.infer<typeof videoSchema>>({
    resolver: zodResolver(videoSchema),
    defaultValues: { displayName: "", externalUrl: "" },
  });

  async function onSubmit(values: z.infer<typeof videoSchema>) {
    setFormError(null);
    try {
      await createLessonResource(lessonId, { resourceType: "VIDEO", ...values });
      toast.success("Video added.");
      router.refresh();
    } catch (error) {
      setFormError(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
    }
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3 rounded-lg border p-3" noValidate>
        {formError && (
          <p className="rounded-md bg-destructive/10 px-3 py-2 text-sm text-destructive" role="alert">
            {formError}
          </p>
        )}
        <FormField
          control={form.control}
          name="displayName"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Video name</FormLabel>
              <FormControl render={<Input {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name="externalUrl"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Video URL</FormLabel>
              <FormControl render={<Input placeholder="https://…" {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />
        <Button type="submit" size="sm" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Adding…" : "Add video"}
        </Button>
      </form>
    </Form>
  );
}

const resourceSchema = z.object({
  resourceType: z.enum(SUPPORTING_RESOURCE_TYPES),
  displayName: z.string().min(1, "Name is required."),
  description: z.string(),
  externalUrl: z.string(),
});

function AddResourceForm({ lessonId }: { lessonId: string }) {
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);
  const [file, setFile] = useState<File | null>(null);

  const form = useForm<z.infer<typeof resourceSchema>>({
    resolver: zodResolver(resourceSchema),
    defaultValues: { resourceType: "EXTERNAL_LINK", displayName: "", description: "", externalUrl: "" },
  });

  const resourceType = form.watch("resourceType");
  const isFileType = (FILE_RESOURCE_TYPES as readonly string[]).includes(resourceType);

  async function onSubmit(values: z.infer<typeof resourceSchema>) {
    setFormError(null);

    if (isFileType && !file) {
      setFormError("Choose a file to upload.");
      return;
    }
    if (!isFileType && !values.externalUrl) {
      setFormError("A URL is required for an external link.");
      return;
    }

    try {
      const fileReference = isFileType && file ? await uploadResourceFile(file) : undefined;
      await createLessonResource(lessonId, {
        resourceType: values.resourceType,
        displayName: values.displayName,
        description: values.description || undefined,
        externalUrl: isFileType ? undefined : values.externalUrl,
        fileReference,
      });
      toast.success("Resource added.");
      form.reset();
      setFile(null);
      router.refresh();
    } catch (error) {
      setFormError(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
    }
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3 rounded-lg border p-3" noValidate>
        {formError && (
          <p className="rounded-md bg-destructive/10 px-3 py-2 text-sm text-destructive" role="alert">
            {formError}
          </p>
        )}

        <FormField
          control={form.control}
          name="resourceType"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Type</FormLabel>
              <FormControl
                render={
                  <select className={selectClassName} {...field}>
                    {SUPPORTING_RESOURCE_TYPES.map((type) => (
                      <option key={type} value={type}>
                        {type}
                      </option>
                    ))}
                  </select>
                }
              />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="displayName"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Name</FormLabel>
              <FormControl render={<Input {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="description"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Description</FormLabel>
              <FormControl render={<Input placeholder="Optional" {...field} />} />
            </FormItem>
          )}
        />

        {isFileType ? (
          <div className="grid gap-2">
            <label className="text-sm font-medium">File</label>
            <input
              type="file"
              accept={ACCEPT_BY_TYPE[resourceType as (typeof FILE_RESOURCE_TYPES)[number]]}
              onChange={(e) => setFile(e.target.files?.[0] ?? null)}
              className="text-sm"
            />
            <p className="text-muted-foreground text-xs">Up to 5 MB.</p>
          </div>
        ) : (
          <FormField
            control={form.control}
            name="externalUrl"
            render={({ field }) => (
              <FormItem>
                <FormLabel>URL</FormLabel>
                <FormControl render={<Input placeholder="https://…" {...field} />} />
                <FormMessage />
              </FormItem>
            )}
          />
        )}

        <Button type="submit" size="sm" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Adding…" : "Add resource"}
        </Button>
      </form>
    </Form>
  );
}
