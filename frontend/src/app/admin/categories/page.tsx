import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { CategoriesFilterBar } from "@/components/categories/categories-filter-bar";
import { PaginationControls } from "@/components/shared/pagination-controls";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { listCategories } from "@/lib/categories/queries";

export const metadata = { title: "Categories | Dhyan Mitra" };

const STATUS_VARIANT = {
  ACTIVE: "default",
  INACTIVE: "secondary",
} as const;

export default async function AdminCategoriesPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const page = Number(params.page ?? 0);
  const search = typeof params.search === "string" ? params.search : undefined;
  const status = typeof params.status === "string" ? (params.status as "ACTIVE" | "INACTIVE") : undefined;

  const result = await listCategories({ page, size: 20, search, status });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold tracking-tight">Categories</h1>
        <Link href="/admin/categories/new" className={cn(buttonVariants())}>
          Create category
        </Link>
      </div>

      <CategoriesFilterBar />

      <Card>
        <CardContent>
          {result.content.length === 0 ? (
            <p className="text-muted-foreground text-sm">No categories match these filters.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Display order</TableHead>
                  <TableHead>Courses</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {result.content.map((category) => (
                  <TableRow key={category.id}>
                    <TableCell className="font-medium">
                      <Link href={`/admin/categories/${category.id}`} className="hover:underline">
                        {category.name}
                      </Link>
                    </TableCell>
                    <TableCell>
                      {category.status && <Badge variant={STATUS_VARIANT[category.status]}>{category.status}</Badge>}
                    </TableCell>
                    <TableCell>{category.displayOrder ?? "—"}</TableCell>
                    <TableCell>{category.courseCount ?? 0}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <PaginationControls page={result.page.page ?? 0} totalPages={result.page.totalPages ?? 1} />
    </div>
  );
}
