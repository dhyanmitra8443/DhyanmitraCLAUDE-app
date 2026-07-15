"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import type { CategorySummary } from "@/lib/api/types";

const DIFFICULTY_OPTIONS = ["BEGINNER", "INTERMEDIATE", "ADVANCED"] as const;
const STATUS_OPTIONS = ["DRAFT", "PUBLISHED", "ARCHIVED"] as const;

const selectClassName =
  "h-8 rounded-lg border border-input bg-transparent px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30";

/** Ref: SRS 5.13, 5.14 - search + category/difficulty/language filters. */
export function CourseFilterBar({
  categories,
  basePath,
  showStatus = false,
}: {
  categories: CategorySummary[];
  basePath: string;
  showStatus?: boolean;
}) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [search, setSearch] = useState(searchParams.get("search") ?? "");
  const selectedCategories = searchParams.getAll("categoryId");

  function updateParams(mutate: (params: URLSearchParams) => void) {
    const params = new URLSearchParams(searchParams.toString());
    mutate(params);
    params.delete("page");
    router.push(`${basePath}?${params.toString()}`);
  }

  function toggleCategory(id: string) {
    updateParams((params) => {
      const current = params.getAll("categoryId");
      params.delete("categoryId");
      const next = current.includes(id) ? current.filter((c) => c !== id) : [...current, id];
      for (const c of next) params.append("categoryId", c);
    });
  }

  return (
    <div className="space-y-3">
      <form
        className="flex flex-wrap items-center gap-2"
        onSubmit={(e) => {
          e.preventDefault();
          updateParams((params) => {
            if (search) params.set("search", search);
            else params.delete("search");
          });
        }}
      >
        <Input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search courses"
          className="max-w-xs"
        />
        <Button type="submit" variant="outline" size="sm">
          Search
        </Button>

        <select
          className={selectClassName}
          value={searchParams.get("difficultyLevel") ?? ""}
          onChange={(e) =>
            updateParams((params) => {
              if (e.target.value) params.set("difficultyLevel", e.target.value);
              else params.delete("difficultyLevel");
            })
          }
        >
          <option value="">All levels</option>
          {DIFFICULTY_OPTIONS.map((level) => (
            <option key={level} value={level}>
              {level}
            </option>
          ))}
        </select>

        <Input
          defaultValue={searchParams.get("language") ?? ""}
          placeholder="Language"
          className="max-w-32"
          onBlur={(e) =>
            updateParams((params) => {
              if (e.target.value) params.set("language", e.target.value);
              else params.delete("language");
            })
          }
        />

        {showStatus && (
          <select
            className={selectClassName}
            value={searchParams.get("status") ?? ""}
            onChange={(e) =>
              updateParams((params) => {
                if (e.target.value) params.set("status", e.target.value);
                else params.delete("status");
              })
            }
          >
            <option value="">All statuses</option>
            {STATUS_OPTIONS.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
        )}
      </form>

      {categories.length > 0 && (
        <div className="flex flex-wrap gap-1.5">
          {categories.map(
            (category) =>
              category.id && (
                <button
                  key={category.id}
                  type="button"
                  onClick={() => toggleCategory(category.id!)}
                  className={
                    selectedCategories.includes(category.id)
                      ? "rounded-full border border-transparent bg-primary px-2.5 py-0.5 text-xs font-medium text-primary-foreground"
                      : "text-muted-foreground hover:bg-muted hover:text-foreground rounded-full border px-2.5 py-0.5 text-xs"
                  }
                >
                  {category.name}
                </button>
              ),
          )}
        </div>
      )}
    </div>
  );
}
