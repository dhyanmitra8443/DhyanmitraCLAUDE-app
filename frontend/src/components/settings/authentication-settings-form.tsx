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
import { updateAuthenticationSettings } from "@/lib/settings/client";
import type { AuthenticationSettings } from "@/lib/api/types";

const schema = z.object({
  sessionTimeoutMinutes: z.string().min(1, "Required."),
  maxLoginAttempts: z.string().min(1, "Required."),
  passwordMinLength: z.string().min(1, "Required."),
  requireUppercase: z.boolean(),
  requireLowercase: z.boolean(),
  requireDigit: z.boolean(),
  requireSpecialChar: z.boolean(),
});

type FormValues = z.infer<typeof schema>;

/**
 * Ref: SRS 16.5 - length can't go below 8, and at least one letter class plus
 * one digit-or-special requirement must always stay mandatory; the backend
 * enforces that floor, this form just surfaces its 400 if violated.
 */
export function AuthenticationSettingsForm({ settings }: { settings: AuthenticationSettings }) {
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      sessionTimeoutMinutes: String(settings.sessionTimeoutMinutes ?? 30),
      maxLoginAttempts: String(settings.maxLoginAttempts ?? 5),
      passwordMinLength: String(settings.passwordMinLength ?? 8),
      requireUppercase: settings.passwordComplexity?.requireUppercase ?? true,
      requireLowercase: settings.passwordComplexity?.requireLowercase ?? true,
      requireDigit: settings.passwordComplexity?.requireDigit ?? true,
      requireSpecialChar: settings.passwordComplexity?.requireSpecialChar ?? true,
    },
  });

  async function onSubmit(values: FormValues) {
    setFormError(null);
    try {
      await updateAuthenticationSettings({
        sessionTimeoutMinutes: Number(values.sessionTimeoutMinutes),
        maxLoginAttempts: Number(values.maxLoginAttempts),
        passwordMinLength: Number(values.passwordMinLength),
        passwordComplexity: {
          requireUppercase: values.requireUppercase,
          requireLowercase: values.requireLowercase,
          requireDigit: values.requireDigit,
          requireSpecialChar: values.requireSpecialChar,
        },
      });
      toast.success("Authentication settings updated.");
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
            name="sessionTimeoutMinutes"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Session timeout (minutes)</FormLabel>
                <FormControl render={<Input type="number" min={1} {...field} />} />
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="maxLoginAttempts"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Max login attempts</FormLabel>
                <FormControl render={<Input type="number" min={1} {...field} />} />
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        <FormField
          control={form.control}
          name="passwordMinLength"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Minimum password length (8–64)</FormLabel>
              <FormControl render={<Input type="number" min={8} max={64} {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <div>
          <p className="mb-2 text-sm font-medium">Password complexity</p>
          <div className="grid grid-cols-2 gap-2">
            {(
              [
                ["requireUppercase", "Require uppercase letter"],
                ["requireLowercase", "Require lowercase letter"],
                ["requireDigit", "Require digit"],
                ["requireSpecialChar", "Require special character"],
              ] as const
            ).map(([name, label]) => (
              <FormField
                key={name}
                control={form.control}
                name={name}
                render={({ field }) => (
                  <label className="flex items-center gap-2 text-sm">
                    <input
                      type="checkbox"
                      checked={field.value}
                      onChange={(e) => field.onChange(e.target.checked)}
                      className="accent-foreground size-4"
                    />
                    {label}
                  </label>
                )}
              />
            ))}
          </div>
          <p className="text-muted-foreground mt-2 text-xs">
            At least one letter class and one digit-or-special requirement must stay enabled.
          </p>
        </div>

        <Button type="submit" size="sm" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Saving…" : "Save changes"}
        </Button>
      </form>
    </Form>
  );
}
