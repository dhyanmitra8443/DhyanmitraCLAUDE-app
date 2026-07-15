"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { ApiError } from "@/lib/api/errors";
import { createCourse, updateCourse } from "@/lib/courses/client";
import type { CategorySummary, CourseDetail, UserSummary } from "@/lib/api/types";

const textareaClassName =
  "w-full rounded-lg border border-input bg-transparent px-2.5 py-1.5 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30";
const selectClassName =
  "h-8 w-full rounded-lg border border-input bg-transparent px-2.5 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30";

const schema = z.object({
  title: z.string().min(1, "Title is required."),
  shortDescription: z.string().min(1, "Short description is required."),
  detailedDescription: z.string().min(1, "Detailed description is required."),
  thumbnailUrl: z.string().min(1, "Thumbnail URL is required.").url("Enter a valid URL."),
  language: z.string().min(1, "Language is required."),
  difficultyLevel: z.enum(["BEGINNER", "INTERMEDIATE", "ADVANCED"]),
  estimatedDurationMinutes: z.string(),
  categoryIds: z.array(z.string()).min(1, "Select at least one category."),
});

type FormValues = z.infer<typeof schema>;

/**
 * Ref: SRS 5.5, 5.11. Handles course create and the plain-field part of
 * edit. Instructor/category *membership* is deliberately NOT editable here
 * after creation: the bulk update endpoint replaces both sets wholesale with
 * no "can't drop the last one from a published course" check - that guard
 * only lives on the dedicated add/remove endpoints, so post-creation
 * membership changes go through CourseInstructorsManager /
 * CourseCategoriesManager instead (Ref: SRS 5.6, 5.7).
 */
export function CourseForm({
  mode,
  role,
  course,
  categories,
  instructors,
  fixedInstructorIds,
}: {
  mode: "create" | "edit";
  role: "admin" | "instructor";
  course?: CourseDetail;
  categories: CategorySummary[];
  instructors?: UserSummary[];
  fixedInstructorIds?: string[];
}) {
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);
  const [instructorIds, setInstructorIds] = useState<string[]>(
    course?.instructors?.map((i) => i.id).filter((id): id is string => Boolean(id)) ??
      fixedInstructorIds ??
      [],
  );

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: course?.title ?? "",
      shortDescription: course?.shortDescription ?? "",
      detailedDescription: course?.detailedDescription ?? "",
      thumbnailUrl: course?.thumbnailUrl ?? "",
      language: course?.language ?? "",
      difficultyLevel: course?.difficultyLevel ?? "BEGINNER",
      estimatedDurationMinutes:
        course?.estimatedDurationMinutes != null ? String(course.estimatedDurationMinutes) : "",
      categoryIds: course?.categories?.map((c) => c.id).filter((id): id is string => Boolean(id)) ?? [],
    },
  });

  const categoryIds = form.watch("categoryIds");

  function toggleCategory(id: string) {
    form.setValue(
      "categoryIds",
      categoryIds.includes(id) ? categoryIds.filter((c) => c !== id) : [...categoryIds, id],
      { shouldValidate: true },
    );
  }

  function toggleInstructor(id: string) {
    setInstructorIds((current) => (current.includes(id) ? current.filter((i) => i !== id) : [...current, id]));
  }

  async function onSubmit(values: FormValues) {
    setFormError(null);

    if (mode === "create" && instructorIds.length === 0) {
      setFormError("Select at least one instructor.");
      return;
    }

    const payload = {
      title: values.title,
      shortDescription: values.shortDescription,
      detailedDescription: values.detailedDescription,
      thumbnailUrl: values.thumbnailUrl,
      language: values.language,
      difficultyLevel: values.difficultyLevel,
      estimatedDurationMinutes: values.estimatedDurationMinutes ? Number(values.estimatedDurationMinutes) : undefined,
      categoryIds: values.categoryIds,
      instructorIds,
    };

    try {
      if (mode === "create") {
        const created = await createCourse(payload);
        toast.success("Course created as a draft.");
        router.push(role === "admin" ? `/admin/courses/${created.id}` : `/teach/courses/${created.id}`);
      } else if (course?.id) {
        await updateCourse(course.id, payload);
        toast.success("Course updated.");
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
              <FormLabel>Detailed description</FormLabel>
              <FormControl render={<textarea {...field} rows={5} className={textareaClassName} />} />
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
              <FormControl render={<Input type="url" placeholder="https://…" {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <div className="grid grid-cols-2 gap-4">
          <FormField
            control={form.control}
            name="language"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Language</FormLabel>
                <FormControl render={<Input placeholder="English" {...field} />} />
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="difficultyLevel"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Difficulty level</FormLabel>
                <FormControl
                  render={
                    <select className={selectClassName} {...field}>
                      <option value="BEGINNER">Beginner</option>
                      <option value="INTERMEDIATE">Intermediate</option>
                      <option value="ADVANCED">Advanced</option>
                    </select>
                  }
                />
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        <FormField
          control={form.control}
          name="estimatedDurationMinutes"
          render={({ field }) => (
            <FormItem className="max-w-48">
              <FormLabel>Estimated duration (minutes)</FormLabel>
              <FormControl render={<Input type="number" min={0} {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <FormItem>
          <FormLabel>Categories</FormLabel>
          <div className="flex flex-wrap gap-1.5">
            {categories.map((category) => (
              <button
                key={category.id}
                type="button"
                onClick={() => category.id && toggleCategory(category.id)}
                className={
                  category.id && categoryIds.includes(category.id)
                    ? "rounded-full border border-transparent bg-primary px-2.5 py-0.5 text-xs font-medium text-primary-foreground"
                    : "text-muted-foreground hover:bg-muted hover:text-foreground rounded-full border px-2.5 py-0.5 text-xs"
                }
              >
                {category.name}
              </button>
            ))}
          </div>
          <FormMessage>{form.formState.errors.categoryIds?.message}</FormMessage>
        </FormItem>

        {mode === "create" && role === "admin" && (
          <FormItem>
            <FormLabel>Instructors</FormLabel>
            <div className="flex flex-wrap gap-1.5">
              {(instructors ?? []).map((instructor) => (
                <button
                  key={instructor.id}
                  type="button"
                  onClick={() => instructor.id && toggleInstructor(instructor.id)}
                  className={
                    instructor.id && instructorIds.includes(instructor.id)
                      ? "rounded-full border border-transparent bg-primary px-2.5 py-0.5 text-xs font-medium text-primary-foreground"
                      : "text-muted-foreground hover:bg-muted hover:text-foreground rounded-full border px-2.5 py-0.5 text-xs"
                  }
                >
                  {instructor.firstName} {instructor.lastName}
                </button>
              ))}
              {(instructors ?? []).length === 0 && (
                <p className="text-muted-foreground text-sm">No instructors available.</p>
              )}
            </div>
          </FormItem>
        )}

        {mode === "create" && role === "instructor" && (
          <FormItem>
            <FormLabel>Instructors</FormLabel>
            <Badge variant="secondary">You</Badge>
          </FormItem>
        )}

        <Button type="submit" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Saving…" : mode === "create" ? "Create course" : "Save changes"}
        </Button>
      </form>
    </Form>
  );
}
