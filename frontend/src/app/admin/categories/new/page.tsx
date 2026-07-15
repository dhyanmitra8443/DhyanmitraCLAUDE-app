import { CategoryForm } from "@/components/categories/category-form";

export const metadata = { title: "Create category | Dhyan Mitra" };

export default function NewCategoryPage() {
  return (
    <div className="max-w-md space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Create category</h1>
      <CategoryForm mode="create" />
    </div>
  );
}
