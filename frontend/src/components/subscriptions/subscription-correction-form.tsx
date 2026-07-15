"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Form, FormControl, FormField, FormItem, FormLabel } from "@/components/ui/form";
import { ApiError } from "@/lib/api/errors";
import { adminUpdateSubscription } from "@/lib/subscriptions/client";
import type { SubscriptionSummary } from "@/lib/api/types";

const selectClassName =
  "h-8 rounded-lg border border-input bg-transparent px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30";

const schema = z.object({
  status: z.enum(["ACTIVE", "EXPIRED", "CANCELLED", "PENDING"]),
  endDate: z.string(),
});

/**
 * Ref: SRS 9.14 - administrator-only. This is a raw overwrite (the admin
 * supplies the new end date directly); it does not run the duration-add
 * "extend" math that only exists in Ch.10's payment webhook path.
 */
export function SubscriptionCorrectionForm({ subscription }: { subscription: SubscriptionSummary }) {
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);

  const form = useForm<z.infer<typeof schema>>({
    resolver: zodResolver(schema),
    defaultValues: {
      status: subscription.status ?? "ACTIVE",
      endDate: subscription.endDate ?? "",
    },
  });

  async function onSubmit(values: z.infer<typeof schema>) {
    setFormError(null);
    try {
      await adminUpdateSubscription(subscription.id!, values);
      toast.success("Subscription updated.");
      router.refresh();
    } catch (error) {
      setFormError(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
    }
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="max-w-sm space-y-4" noValidate>
        {formError && (
          <p className="rounded-md bg-destructive/10 px-3 py-2 text-sm text-destructive" role="alert">
            {formError}
          </p>
        )}

        <FormField
          control={form.control}
          name="status"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Status</FormLabel>
              <FormControl
                render={
                  <select className={selectClassName} {...field}>
                    <option value="ACTIVE">ACTIVE</option>
                    <option value="EXPIRED">EXPIRED</option>
                    <option value="CANCELLED">CANCELLED</option>
                    <option value="PENDING">PENDING</option>
                  </select>
                }
              />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="endDate"
          render={({ field }) => (
            <FormItem>
              <FormLabel>End date</FormLabel>
              <FormControl render={<Input type="date" {...field} />} />
            </FormItem>
          )}
        />

        <Button type="submit" size="sm" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Saving…" : "Save correction"}
        </Button>
      </form>
    </Form>
  );
}
