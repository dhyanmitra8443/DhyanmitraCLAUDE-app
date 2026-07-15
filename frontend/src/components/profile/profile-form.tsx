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
import { Badge } from "@/components/ui/badge";
import { ApiError } from "@/lib/api/errors";
import { adminUpdateUser, updateOwnProfile } from "@/lib/users/client";
import type { UpdateOwnProfileRequest, UserProfile } from "@/lib/api/types";

const SPECIALIZATION_SUGGESTIONS = ["Yoga", "Meditation", "Pranayama", "Therapy Yoga", "Mindfulness"];

const schema = z.object({
  firstName: z.string().min(1, "First name is required."),
  lastName: z.string().min(1, "Last name is required."),
  mobileNumber: z.string().min(1, "Mobile number is required."),
  dateOfBirth: z.string(),
  gender: z.string(),
  professionalBio: z.string(),
  yearsOfExperience: z.string(),
  specializations: z.array(z.string()),
});

type FormValues = z.infer<typeof schema>;

/**
 * Shared by the self-service profile page and the administrator's user-edit
 * view (Ref: SRS 4.6, 4.7, 4.5). Instructor-only fields only render when the
 * profile being edited belongs to an instructor - the backend already drops
 * them for other roles, this just avoids showing controls that do nothing.
 */
type ProfileFormProps = { profile: UserProfile } & ({ mode: "own" } | { mode: "admin"; userId: string });

export function ProfileForm(props: ProfileFormProps) {
  const { profile } = props;
  const save = (payload: UpdateOwnProfileRequest) =>
    props.mode === "own" ? updateOwnProfile(payload) : adminUpdateUser(props.userId, payload);
  const isInstructor = profile.role === "INSTRUCTOR";
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);
  const [specializationInput, setSpecializationInput] = useState("");

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      firstName: profile.firstName ?? "",
      lastName: profile.lastName ?? "",
      mobileNumber: profile.mobileNumber ?? "",
      dateOfBirth: profile.dateOfBirth ?? "",
      gender: profile.gender ?? "",
      professionalBio: profile.professionalBio ?? "",
      yearsOfExperience: profile.yearsOfExperience != null ? String(profile.yearsOfExperience) : "",
      specializations: profile.specializations ?? [],
    },
  });

  const specializations = form.watch("specializations");

  function addSpecialization(value: string) {
    const trimmed = value.trim();
    if (!trimmed || specializations.includes(trimmed)) return;
    form.setValue("specializations", [...specializations, trimmed]);
    setSpecializationInput("");
  }

  function removeSpecialization(value: string) {
    form.setValue(
      "specializations",
      specializations.filter((s) => s !== value),
    );
  }

  async function onSubmit(values: FormValues) {
    setFormError(null);
    const payload: UpdateOwnProfileRequest = {
      firstName: values.firstName,
      lastName: values.lastName,
      mobileNumber: values.mobileNumber,
      ...(values.dateOfBirth ? { dateOfBirth: values.dateOfBirth } : {}),
      ...(values.gender ? { gender: values.gender } : {}),
      ...(isInstructor
        ? {
            professionalBio: values.professionalBio,
            yearsOfExperience: values.yearsOfExperience ? Number(values.yearsOfExperience) : undefined,
            specializations: values.specializations,
          }
        : {}),
    };

    try {
      await save(payload);
      toast.success("Profile updated.");
      router.refresh();
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

        <div className="grid grid-cols-2 gap-4">
          <FormItem>
            <FormLabel>Email</FormLabel>
            <Input value={profile.email ?? ""} disabled />
          </FormItem>
          <FormItem>
            <FormLabel>Role</FormLabel>
            <Input value={profile.role ?? ""} disabled />
          </FormItem>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <FormField
            control={form.control}
            name="firstName"
            render={({ field }) => (
              <FormItem>
                <FormLabel>First name</FormLabel>
                <FormControl render={<Input autoComplete="given-name" {...field} />} />
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="lastName"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Last name</FormLabel>
                <FormControl render={<Input autoComplete="family-name" {...field} />} />
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        <FormField
          control={form.control}
          name="mobileNumber"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Mobile number</FormLabel>
              <FormControl render={<Input type="tel" autoComplete="tel" {...field} />} />
              <FormMessage />
            </FormItem>
          )}
        />

        <div className="grid grid-cols-2 gap-4">
          <FormField
            control={form.control}
            name="dateOfBirth"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Date of birth</FormLabel>
                <FormControl render={<Input type="date" {...field} />} />
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="gender"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Gender</FormLabel>
                <FormControl render={<Input placeholder="Optional" {...field} />} />
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        {isInstructor && (
          <>
            <FormField
              control={form.control}
              name="professionalBio"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Professional biography</FormLabel>
                  <FormControl
                    render={
                      <textarea
                        {...field}
                        rows={4}
                        className="w-full rounded-lg border border-input bg-transparent px-2.5 py-1.5 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30"
                      />
                    }
                  />
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="yearsOfExperience"
              render={({ field }) => (
                <FormItem className="max-w-40">
                  <FormLabel>Years of experience</FormLabel>
                  <FormControl render={<Input type="number" min={0} {...field} />} />
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormItem>
              <FormLabel>Specializations</FormLabel>
              <div className="flex flex-wrap gap-2">
                {specializations.map((value) => (
                  <Badge key={value} variant="secondary" className="gap-1">
                    {value}
                    <button
                      type="button"
                      onClick={() => removeSpecialization(value)}
                      className="text-muted-foreground hover:text-foreground"
                      aria-label={`Remove ${value}`}
                    >
                      ×
                    </button>
                  </Badge>
                ))}
              </div>
              <div className="flex flex-wrap gap-1.5">
                {SPECIALIZATION_SUGGESTIONS.filter((s) => !specializations.includes(s)).map((suggestion) => (
                  <button
                    key={suggestion}
                    type="button"
                    onClick={() => addSpecialization(suggestion)}
                    className="text-muted-foreground hover:bg-muted hover:text-foreground rounded-full border px-2 py-0.5 text-xs"
                  >
                    + {suggestion}
                  </button>
                ))}
              </div>
              <div className="flex gap-2">
                <Input
                  value={specializationInput}
                  onChange={(e) => setSpecializationInput(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter") {
                      e.preventDefault();
                      addSpecialization(specializationInput);
                    }
                  }}
                  placeholder="Add another specialization"
                />
                <Button type="button" variant="outline" onClick={() => addSpecialization(specializationInput)}>
                  Add
                </Button>
              </div>
            </FormItem>
          </>
        )}

        <Button type="submit" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Saving…" : "Save changes"}
        </Button>
      </form>
    </Form>
  );
}
