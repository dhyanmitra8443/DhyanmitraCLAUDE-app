"use client";

import { api } from "@/lib/api/client";
import type { CategorySummary, CreateCategoryRequest } from "@/lib/api/types";

/** Ref: SRS 6.5 - administrator-only. Defaults to ACTIVE. */
export function createCategory(payload: CreateCategoryRequest): Promise<CategorySummary> {
  return api.post<CategorySummary>("/categories", payload);
}

/** Ref: SRS 6.7 - administrator-only; reuses the create DTO (full replace). */
export function updateCategory(categoryId: string, payload: CreateCategoryRequest): Promise<CategorySummary> {
  return api.patch<CategorySummary>(`/categories/${categoryId}`, payload);
}

/** Ref: SRS 6.8 - categories are never deleted, only (de)activated. */
export function updateCategoryStatus(categoryId: string, status: "ACTIVE" | "INACTIVE"): Promise<null> {
  return api.patch<null>(`/categories/${categoryId}/status`, { status });
}
