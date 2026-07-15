"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Skeleton } from "@/components/ui/skeleton";
import { ApiError } from "@/lib/api/errors";
import { acceptInvitation, previewInvitation } from "@/lib/auth/client";

const schema = z
  .object({
    password: z.string().min(8, "Password must be at least 8 characters."),
    confirmPassword: z.string().min(1, "Please confirm your password."),
  })
  .refine((values) => values.password === values.confirmPassword, {
    path: ["confirmPassword"],
    message: "Passwords do not match.",
  });

type FormValues = z.infer<typeof schema>;

export function AcceptInvitationForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const token = searchParams.get("token");

  const [preview, setPreview] = useState<{ email: string; firstName: string; lastName: string } | null>(null);
  const [previewError, setPreviewError] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) return;
    previewInvitation(token)
      .then(setPreview)
      .catch((error) => {
        setPreviewError(error instanceof ApiError ? error.message : "This invitation link is invalid or has expired.");
      });
  }, [token]);

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { password: "", confirmPassword: "" },
  });

  async function onSubmit(values: FormValues) {
    if (!token) return;
    setFormError(null);
    try {
      await acceptInvitation({ token, ...values });
      toast.success("Account activated. Sign in to continue.");
      router.push("/sign-in");
    } catch (error) {
      if (error instanceof ApiError) {
        for (const [field, message] of Object.entries(error.fieldErrors)) {
          if (field === "password" || field === "confirmPassword") form.setError(field, { message });
        }
        setFormError(error.message);
      } else {
        setFormError("Something went wrong. Please try again.");
      }
    }
  }

  if (!token) {
    return (
      <p className="rounded-md bg-destructive/10 px-3 py-2 text-center text-sm text-destructive">
        This link is missing its invitation token.
      </p>
    );
  }

  if (previewError) {
    return (
      <p className="rounded-md bg-destructive/10 px-3 py-2 text-center text-sm text-destructive">
        {previewError} Ask your administrator to send a new invitation.
      </p>
    );
  }

  if (!preview) {
    return (
      <div className="space-y-3">
        <Skeleton className="h-9 w-full" />
        <Skeleton className="h-9 w-full" />
        <Skeleton className="h-9 w-full" />
      </div>
    );
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <p className="text-muted-foreground text-sm">
          Setting up <span className="text-foreground font-medium">{preview.firstName} {preview.lastName}</span> (
          {preview.email})
        </p>

        {formError && (
          <p className="rounded-md bg-destructive/10 px-3 py-2 text-sm text-destructive" role="alert">
            {formError}
          </p>
        )}

        <FormField
          control={form.control}
          name="password"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Password</FormLabel>
              <FormControl render={<Input type="password" autoComplete="new-password" {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="confirmPassword"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Confirm password</FormLabel>
              <FormControl render={<Input type="password" autoComplete="new-password" {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <Button type="submit" size="lg" className="w-full" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Activating…" : "Activate account"}
        </Button>
      </form>
    </Form>
  );
}
