"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Form, FormField } from "@/components/ui/form";
import { ApiError } from "@/lib/api/errors";
import { updateNotificationSettings } from "@/lib/settings/client";
import type { NotificationSettings } from "@/lib/api/types";

interface FormValues {
  emailNotificationsEnabled: boolean;
  inAppNotificationsEnabled: boolean;
}

/** Ref: SRS 16.8 - disabling a channel affects future notifications only, existing ones are untouched. */
export function NotificationSettingsForm({ settings }: { settings: NotificationSettings }) {
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);

  const form = useForm<FormValues>({
    defaultValues: {
      emailNotificationsEnabled: settings.emailNotificationsEnabled ?? true,
      inAppNotificationsEnabled: settings.inAppNotificationsEnabled ?? true,
    },
  });

  async function onSubmit(values: FormValues) {
    setFormError(null);
    try {
      await updateNotificationSettings(values);
      toast.success("Notification settings updated.");
      router.refresh();
    } catch (error) {
      setFormError(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
    }
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3" noValidate>
        {formError && (
          <p className="rounded-md bg-destructive/10 px-3 py-2 text-sm text-destructive" role="alert">
            {formError}
          </p>
        )}

        <div className="space-y-2">
          <FormField
            control={form.control}
            name="emailNotificationsEnabled"
            render={({ field }) => (
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={field.value}
                  onChange={(e) => field.onChange(e.target.checked)}
                  className="accent-foreground size-4"
                />
                Email notifications enabled
              </label>
            )}
          />
          <FormField
            control={form.control}
            name="inAppNotificationsEnabled"
            render={({ field }) => (
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={field.value}
                  onChange={(e) => field.onChange(e.target.checked)}
                  className="accent-foreground size-4"
                />
                In-app notifications enabled
              </label>
            )}
          />
        </div>

        <Button type="submit" size="sm" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Saving…" : "Save changes"}
        </Button>
      </form>
    </Form>
  );
}
