import { notFound } from "next/navigation";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { CategoryForm } from "@/components/categories/category-form";
import { CategoryStatusAction } from "@/components/categories/category-status-action";
import { getCategoryDetail } from "@/lib/categories/queries";
import { ApiError } from "@/lib/api/errors";

export const metadata = { title: "Edit category | Dhyan Mitra" };

const STATUS_VARIANT = {
  ACTIVE: "default",
  INACTIVE: "secondary",
} as const;

export default async function AdminCategoryDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  let category;
  try {
    category = await getCategoryDetail(id);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) notFound();
    throw error;
  }

  return (
    <div className="max-w-md space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">{category.name}</h1>
          <div className="mt-1 flex items-center gap-2">
            {category.status && <Badge variant={STATUS_VARIANT[category.status]}>{category.status}</Badge>}
            <span className="text-muted-foreground text-sm">{category.courseCount ?? 0} courses</span>
          </div>
        </div>
        {category.status && <CategoryStatusAction categoryId={id} status={category.status} />}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Details</CardTitle>
        </CardHeader>
        <CardContent>
          <CategoryForm mode="edit" category={category} />
        </CardContent>
      </Card>
    </div>
  );
}
