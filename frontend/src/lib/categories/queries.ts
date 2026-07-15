import "server-only";

import { fetchFromBackend } from "@/lib/api/server";
import type { CategorySummary, Paginated } from "@/lib/api/types";

export interface ListCategoriesParams {
  page?: number;
  size?: number;
  search?: string;
  /** Administrator-only filter (Ref: SRS 6.9). */
  status?: "ACTIVE" | "INACTIVE";
}

/**
 * Ref: SRS 6.9, 6.10 - public/non-admin callers only see ACTIVE categories
 * that already have at least one published course; administrators see all.
 */
export function listCategories(params: ListCategoriesParams = {}): Promise<Paginated<CategorySummary>> {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") query.set(key, String(value));
  }
  const qs = query.toString();
  return fetchFromBackend<Paginated<CategorySummary>>(`/api/v1/categories${qs ? `?${qs}` : ""}`);
}

/** Ref: SRS 6.3. */
export function getCategoryDetail(categoryId: string): Promise<CategorySummary> {
  return fetchFromBackend<CategorySummary>(`/api/v1/categories/${categoryId}`);
}
