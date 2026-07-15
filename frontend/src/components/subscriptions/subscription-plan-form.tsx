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
import { createSubscriptionPlan, updateSubscriptionPlan } from "@/lib/subscriptions/client";
import type { SubscriptionPlanSummary } from "@/lib/api/types";

const selectClassName =
  "h-8 rounded-lg border border-input bg-transparent px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30";

const schema = z.object({
  planName: z.string().min(1, "Name is required."),
  description: z.string(),
  price: z.string().min(1, "Price is required."),
  currency: z.string().min(1, "Currency is required."),
  duration: z.string().min(1, "Duration is required."),
  durationUnit: z.enum(["DAY", "MONTH", "YEAR"]),
});

type FormValues = z.infer<typeof schema>;

/** Ref: SRS 9.3, 9.15 - administrator-only create/edit of a course's subscription plans. */
export function SubscriptionPlanForm({
  courseId,
  mode,
  plan,
  onDone,
}: {
  courseId: string;
  mode: "create" | "edit";
  plan?: SubscriptionPlanSummary;
  onDone?: () => void;
}) {
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      planName: plan?.planName ?? "",
      description: plan?.description ?? "",
      price: plan?.price != null ? String(plan.price) : "",
      currency: plan?.currency ?? "INR",
      duration: plan?.duration != null ? String(plan.duration) : "",
      durationUnit: plan?.durationUnit ?? "MONTH",
    },
  });

  async function onSubmit(values: FormValues) {
    setFormError(null);
    const payload = {
      planName: values.planName,
      description: values.description || undefined,
      price: Number(values.price),
      currency: values.currency,
      duration: Number(values.duration),
      durationUnit: values.durationUnit,
    };

    try {
      if (mode === "create") {
        await createSubscriptionPlan(courseId, payload);
        toast.success("Plan created.");
        form.reset();
      } else if (plan?.id) {
        await updateSubscriptionPlan(plan.id, payload);
        toast.success("Plan updated.");
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
          name="planName"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Plan name</FormLabel>
              <FormControl render={<Input placeholder="Monthly" {...field} />} />
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
            name="price"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Price</FormLabel>
                <FormControl render={<Input type="number" min={0} step="0.01" {...field} />} />
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="currency"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Currency</FormLabel>
                <FormControl render={<Input {...field} />} />
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        <div className="grid grid-cols-2 gap-3">
          <FormField
            control={form.control}
            name="duration"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Duration</FormLabel>
                <FormControl render={<Input type="number" min={1} {...field} />} />
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="durationUnit"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Unit</FormLabel>
                <FormControl
                  render={
                    <select className={selectClassName} {...field}>
                      <option value="DAY">DAY</option>
                      <option value="MONTH">MONTH</option>
                      <option value="YEAR">YEAR</option>
                    </select>
                  }
                />
              </FormItem>
            )}
          />
        </div>

        <Button type="submit" size="sm" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Saving…" : mode === "create" ? "Add plan" : "Save changes"}
        </Button>
      </form>
    </Form>
  );
}
