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
import { updateCertificateSettings } from "@/lib/settings/client";
import type { CertificateSettings } from "@/lib/api/types";

const schema = z.object({
  organizationName: z.string().max(200),
  organizationLogoUrl: z.string(),
  signatureImageUrl: z.string(),
  footerText: z.string(),
});

type FormValues = z.infer<typeof schema>;

/**
 * Ref: SRS 16.11 - applies only to newly generated certificates; already
 * issued certificates snapshot their branding at issuance and never change.
 */
export function CertificateSettingsForm({ settings }: { settings: CertificateSettings }) {
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      organizationName: settings.organizationName ?? "",
      organizationLogoUrl: settings.organizationLogoUrl ?? "",
      signatureImageUrl: settings.signatureImageUrl ?? "",
      footerText: settings.footerText ?? "",
    },
  });

  async function onSubmit(values: FormValues) {
    setFormError(null);
    try {
      await updateCertificateSettings({
        organizationName: values.organizationName || undefined,
        organizationLogoUrl: values.organizationLogoUrl || undefined,
        signatureImageUrl: values.signatureImageUrl || undefined,
        footerText: values.footerText || undefined,
      });
      toast.success("Certificate settings updated.");
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
        <p className="text-muted-foreground text-xs">
          Applies only to certificates issued after this change — already-issued certificates keep their original
          branding.
        </p>

        <FormField
          control={form.control}
          name="organizationName"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Organization name</FormLabel>
              <FormControl render={<Input placeholder="Dhyan Mitra" {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="organizationLogoUrl"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Logo URL</FormLabel>
              <FormControl render={<Input placeholder="https://…" {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="signatureImageUrl"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Signature image URL</FormLabel>
              <FormControl render={<Input placeholder="https://…" {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="footerText"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Footer text</FormLabel>
              <FormControl render={<Input {...field} />} />
              <FormMessage />
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
