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
import { updatePaymentGatewaySettings } from "@/lib/settings/client";
import type { PaymentGatewaySettingsView } from "@/lib/api/types";

const selectClassName =
  "h-8 rounded-lg border border-input bg-transparent px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30";

const schema = z.object({
  razorpayKeyId: z.string(),
  razorpayKeySecret: z.string(),
  razorpayWebhookSecret: z.string(),
  webhookCallbackUrl: z.string(),
  environment: z.enum(["SANDBOX", "PRODUCTION"]),
});

type FormValues = z.infer<typeof schema>;

/** Ref: SRS 16.9 - administrator-only Razorpay configuration. Secret fields are write-only; blank leaves them unchanged. */
export function PaymentGatewayForm({ settings }: { settings: PaymentGatewaySettingsView }) {
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      razorpayKeyId: settings.razorpayKeyId ?? "",
      razorpayKeySecret: "",
      razorpayWebhookSecret: "",
      webhookCallbackUrl: settings.webhookCallbackUrl ?? "",
      environment: settings.environment ?? "SANDBOX",
    },
  });

  async function onSubmit(values: FormValues) {
    setFormError(null);
    try {
      await updatePaymentGatewaySettings({
        razorpayKeyId: values.razorpayKeyId || undefined,
        razorpayKeySecret: values.razorpayKeySecret || undefined,
        razorpayWebhookSecret: values.razorpayWebhookSecret || undefined,
        webhookCallbackUrl: values.webhookCallbackUrl || undefined,
        environment: values.environment,
      });
      toast.success("Payment gateway settings updated.");
      form.setValue("razorpayKeySecret", "");
      form.setValue("razorpayWebhookSecret", "");
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

        <FormField
          control={form.control}
          name="razorpayKeyId"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Razorpay Key ID</FormLabel>
              <FormControl render={<Input placeholder="rzp_test_..." {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="razorpayKeySecret"
          render={({ field }) => (
            <FormItem>
              <FormLabel>
                Razorpay Key Secret{" "}
                <span className="text-muted-foreground font-normal">
                  ({settings.razorpayKeySecretConfigured ? "configured" : "not set"} — leave blank to keep unchanged)
                </span>
              </FormLabel>
              <FormControl render={<Input type="password" autoComplete="off" {...field} />} />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="razorpayWebhookSecret"
          render={({ field }) => (
            <FormItem>
              <FormLabel>
                Razorpay Webhook Secret{" "}
                <span className="text-muted-foreground font-normal">
                  ({settings.razorpayWebhookSecretConfigured ? "configured" : "not set"} — leave blank to keep unchanged)
                </span>
              </FormLabel>
              <FormControl render={<Input type="password" autoComplete="off" {...field} />} />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="webhookCallbackUrl"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Webhook callback URL</FormLabel>
              <FormControl render={<Input placeholder="https://api.example.com/api/v1/payments/razorpay/webhook" {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="environment"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Environment</FormLabel>
              <FormControl
                render={
                  <select className={selectClassName} {...field}>
                    <option value="SANDBOX">SANDBOX</option>
                    <option value="PRODUCTION">PRODUCTION</option>
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
