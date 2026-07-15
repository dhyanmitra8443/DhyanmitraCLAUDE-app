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
import { createSection, updateSection } from "@/lib/sections/client";
import type { SectionDetail } from "@/lib/api/types";

const schema = z.object({
  title: z.string().min(1, "Title is required."),
  shortDescription: z.string(),
});

type FormValues = z.infer<typeof schema>;

/** Ref: SRS 7.4, 7.16 - display order is managed separately via reorder controls, not this form. */
export function SectionForm({
  mode,
  courseId,
  section,
  basePath,
}: {
  mode: "create" | "edit";
  courseId: string;
  section?: SectionDetail;
  /** Area prefix (e.g. "/admin/courses/{courseId}") the newly created section's detail page lives under. */
  basePath?: string;
}) {
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: section?.title ?? "",
      shortDescription: section?.shortDescription ?? "",
    },
  });

  async function onSubmit(values: FormValues) {
    setFormError(null);
    const payload = { title: values.title, shortDescription: values.shortDescription || undefined };

    try {
      if (mode === "create") {
        const created = await createSection(courseId, payload);
        toast.success("Section created as a draft.");
        router.push(basePath ? `${basePath}/sections/${created.id}` : `/admin/courses/${courseId}`);
      } else if (section?.id) {
        await updateSection(section.id, payload);
        toast.success("Section updated.");
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
          name="shortDescription"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Short description</FormLabel>
              <FormControl render={<Input placeholder="Optional" {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <Button type="submit" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Saving…" : mode === "create" ? "Create section" : "Save changes"}
        </Button>
      </form>
    </Form>
  );
}
