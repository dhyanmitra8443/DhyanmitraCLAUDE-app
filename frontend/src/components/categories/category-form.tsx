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
import { createCategory, updateCategory } from "@/lib/categories/client";
import type { CategorySummary } from "@/lib/api/types";

const schema = z.object({
  name: z.string().min(1, "Name is required."),
  description: z.string(),
  iconUrl: z.string(),
  displayOrder: z.string(),
});

type FormValues = z.infer<typeof schema>;

/** Ref: SRS 6.5, 6.7 - administrator-only create/edit. */
export function CategoryForm({ mode, category }: { mode: "create" | "edit"; category?: CategorySummary }) {
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      name: category?.name ?? "",
      description: category?.description ?? "",
      iconUrl: category?.iconUrl ?? "",
      displayOrder: category?.displayOrder != null ? String(category.displayOrder) : "",
    },
  });

  async function onSubmit(values: FormValues) {
    setFormError(null);
    const payload = {
      name: values.name,
      description: values.description || undefined,
      iconUrl: values.iconUrl || undefined,
      displayOrder: values.displayOrder ? Number(values.displayOrder) : undefined,
    };

    try {
      if (mode === "create") {
        const created = await createCategory(payload);
        toast.success("Category created.");
        router.push(`/admin/categories/${created.id}`);
      } else if (category?.id) {
        await updateCategory(category.id, payload);
        toast.success("Category updated.");
        router.refresh();
      }
    } catch (error) {
      if (error instanceof ApiError) {
        for (const [field, message] of Object.entries(error.fieldErrors)) {
          form.setError(field as keyof FormValues, { message });
        }
        setFormError(error.message);
      } else {
        setFormError("Something went wrong. Please try again.");
      }
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
          name="name"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Name</FormLabel>
              <FormControl render={<Input {...field} />} />
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
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="iconUrl"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Icon URL</FormLabel>
              <FormControl render={<Input type="url" placeholder="https://… (optional)" {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="displayOrder"
          render={({ field }) => (
            <FormItem className="max-w-48">
              <FormLabel>Display order</FormLabel>
              <FormControl render={<Input type="number" min={1} placeholder="Optional" {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <Button type="submit" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Saving…" : mode === "create" ? "Create category" : "Save changes"}
        </Button>
      </form>
    </Form>
  );
}
