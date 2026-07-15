"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { ApiError } from "@/lib/api/errors";
import { createLesson, updateLesson } from "@/lib/lessons/client";
import type { LessonDetail } from "@/lib/api/types";

const textareaClassName =
  "w-full rounded-lg border border-input bg-transparent px-2.5 py-1.5 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30";

const schema = z.object({
  title: z.string().min(1, "Title is required."),
  detailedDescription: z.string(),
  videoUrl: z.string().min(1, "Video URL is required."),
  thumbnailUrl: z.string(),
});

type FormValues = z.infer<typeof schema>;

/**
 * Ref: SRS 7.7, 7.15. videoUrl is required here but note: the backend does
 * NOT validate it as a real URL, and publishing a lesson doesn't even look
 * at this field - it requires a separate active VIDEO resource (Ch.8).
 * Display order is managed separately via reorder controls, not this form.
 */
export function LessonForm({
  mode,
  sectionId,
  lesson,
  basePath,
}: {
  mode: "create" | "edit";
  sectionId: string;
  lesson?: LessonDetail;
  /** Area prefix (e.g. "/admin/courses/{courseId}/sections/{sectionId}") the newly created lesson's detail page lives under. */
  basePath?: string;
}) {
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: lesson?.title ?? "",
      detailedDescription: lesson?.detailedDescription ?? "",
      videoUrl: lesson?.videoUrl ?? "",
      thumbnailUrl: lesson?.thumbnailUrl ?? "",
    },
  });

  async function onSubmit(values: FormValues) {
    setFormError(null);
    const payload = {
      title: values.title,
      detailedDescription: values.detailedDescription || undefined,
      videoUrl: values.videoUrl,
      thumbnailUrl: values.thumbnailUrl || undefined,
    };

    try {
      if (mode === "create") {
        const created = await createLesson(sectionId, payload);
        toast.success("Lesson created as a draft.");
        router.push(basePath ? `${basePath}/lessons/${created.id}` : `/admin/courses`);
      } else if (lesson?.id) {
        await updateLesson(lesson.id, payload);
        toast.success("Lesson updated.");
        router.refresh();
      }
    } catch (error) {
      setFormError(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
    }
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4" noValidate>
        {formError && (
          <p className="rounded-md bg-destructive/10 px-3 py-2 text-sm text-destructive" role="alert">
            {formError}
          </p>
        )}

        <FormField
          control={form.control}
          name="title"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Title</FormLabel>
              <FormControl render={<Input {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="detailedDescription"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Description</FormLabel>
              <FormControl render={<textarea {...field} rows={4} className={textareaClassName} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="videoUrl"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Video URL</FormLabel>
              <FormControl render={<Input placeholder="https://…" {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="thumbnailUrl"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Thumbnail URL</FormLabel>
              <FormControl render={<Input placeholder="https://… (optional)" {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <Button type="submit" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Saving…" : mode === "create" ? "Create lesson" : "Save changes"}
        </Button>
      </form>
    </Form>
  );
}
