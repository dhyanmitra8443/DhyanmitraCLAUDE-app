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
import { updateEmailSettings } from "@/lib/settings/client";
import type { EmailSettingsView } from "@/lib/api/types";

const selectClassName =
  "h-8 rounded-lg border border-input bg-transparent px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30";

const schema = z.object({
  smtpHost: z.string(),
  smtpPort: z.string(),
  senderEmail: z.string().email().or(z.literal("")),
  senderDisplayName: z.string(),
  smtpUsername: z.string(),
  smtpPassword: z.string(),
  encryptionType: z.enum(["SSL", "TLS"]),
});

type FormValues = z.infer<typeof schema>;

/** Ref: SRS 16.7 - administrator-only SMTP configuration. smtpPassword is write-only; blank leaves it unchanged. */
export function EmailSettingsForm({ settings }: { settings: EmailSettingsView }) {
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      smtpHost: settings.smtpHost ?? "",
      smtpPort: String(settings.smtpPort ?? 587),
      senderEmail: settings.senderEmail ?? "",
      senderDisplayName: settings.senderDisplayName ?? "",
      smtpUsername: settings.smtpUsername ?? "",
      smtpPassword: "",
      encryptionType: settings.encryptionType ?? "TLS",
    },
  });

  async function onSubmit(values: FormValues) {
    setFormError(null);
    try {
      await updateEmailSettings({
        smtpHost: values.smtpHost || undefined,
        smtpPort: Number(values.smtpPort),
        senderEmail: values.senderEmail || undefined,
        senderDisplayName: values.senderDisplayName || undefined,
        smtpUsername: values.smtpUsername || undefined,
        smtpPassword: values.smtpPassword || undefined,
        encryptionType: values.encryptionType,
      });
      toast.success("Email settings updated.");
      form.setValue("smtpPassword", "");
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

        <div className="grid grid-cols-2 gap-3">
          <FormField
            control={form.control}
            name="smtpHost"
            render={({ field }) => (
              <FormItem>
                <FormLabel>SMTP host</FormLabel>
                <FormControl render={<Input placeholder="smtp.example.com" {...field} />} />
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="smtpPort"
            render={({ field }) => (
              <FormItem>
                <FormLabel>SMTP port</FormLabel>
                <FormControl render={<Input type="number" min={1} max={65535} {...field} />} />
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        <FormField
          control={form.control}
          name="senderEmail"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Sender email</FormLabel>
              <FormControl render={<Input placeholder="no-reply@example.com" {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="senderDisplayName"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Sender display name</FormLabel>
              <FormControl render={<Input placeholder="Dhyan Mitra" {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="smtpUsername"
          render={({ field }) => (
            <FormItem>
              <FormLabel>SMTP username</FormLabel>
              <FormControl render={<Input {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="smtpPassword"
          render={({ field }) => (
            <FormItem>
              <FormLabel>
                SMTP password{" "}
                <span className="text-muted-foreground font-normal">
                  ({settings.smtpPasswordConfigured ? "configured" : "not set"} — leave blank to keep unchanged)
                </span>
              </FormLabel>
              <FormControl render={<Input type="password" autoComplete="off" {...field} />} />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="encryptionType"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Encryption</FormLabel>
              <FormControl
                render={
                  <select className={selectClassName} {...field}>
                    <option value="SSL">SSL</option>
                    <option value="TLS">TLS</option>
                  </select>
                }
              />
            </FormItem>
          )}
        />

        <Button type="submit" size="sm" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Saving…" : "Save changes"}
        </Button>
      </form>
    </Form>
  );
}
