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
import { createLiveClass, updateLiveClass } from "@/lib/live-classes/client";
import type { LiveClassSummary } from "@/lib/api/types";

const schema = z.object({
  title: z.string().min(1, "Title is required."),
  description: z.string(),
  scheduledDate: z.string().min(1, "Date is required."),
  scheduledTime: z.string().min(1, "Time is required."),
  meetingUrl: z.string().min(1, "Meeting URL is required."),
  meetingPassword: z.string(),
});

type FormValues = z.infer<typeof schema>;

/** Ref: SRS 11.6, 11.7, 11.12 - administrator or assigned instructor create/edit of a course's live classes. */
export function LiveClassForm({
  courseId,
  mode,
  liveClass,
  onDone,
}: {
  courseId: string;
  mode: "create" | "edit";
  liveClass?: LiveClassSummary;
  onDone?: () => void;
}) {
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: liveClass?.title ?? "",
      description: liveClass?.description ?? "",
      scheduledDate: liveClass?.scheduledDate ?? "",
      scheduledTime: liveClass?.scheduledTime ?? "",
      meetingUrl: liveClass?.meetingUrl ?? "",
      meetingPassword: "",
    },
  });

  async function onSubmit(values: FormValues) {
    setFormError(null);
    const payload = {
      title: values.title,
      description: values.description || undefined,
      scheduledDate: values.scheduledDate,
      scheduledTime: values.scheduledTime,
      meetingUrl: values.meetingUrl,
      meetingPassword: values.meetingPassword || undefined,
    };

    try {
      if (mode === "create") {
        await createLiveClass(courseId, payload);
        toast.success("Live class scheduled.");
        form.reset();
      } else if (liveClass?.id) {
        await updateLiveClass(liveClass.id, payload);
        toast.success("Live class updated.");
      }
      router.refresh();
      onDone?.();
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
          name="title"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Title</FormLabel>
              <FormControl render={<Input placeholder="Morning Vinyasa Flow" {...field} />} />
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

        <div className="grid grid-cols-2 gap-3">
          <FormField
            control={form.control}
            name="scheduledDate"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Date</FormLabel>
                <FormControl render={<Input type="date" {...field} />} />
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="scheduledTime"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Time</FormLabel>
                <FormControl render={<Input type="time" {...field} />} />
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        <FormField
          control={form.control}
          name="meetingUrl"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Meeting URL</FormLabel>
              <FormControl render={<Input placeholder="https://..." {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="meetingPassword"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Meeting password</FormLabel>
              <FormControl render={<Input placeholder="Optional" {...field} />} />
            </FormItem>
          )}
        />

        <div className="flex gap-2">
          <Button type="submit" size="sm" disabled={form.formState.isSubmitting}>
            {form.formState.isSubmitting ? "Saving…" : mode === "create" ? "Schedule class" : "Save changes"}
          </Button>
          {mode === "edit" && onDone && (
            <Button type="button" size="sm" variant="outline" onClick={onDone}>
              Cancel
            </Button>
          )}
        </div>
      </form>
    </Form>
  );
}
