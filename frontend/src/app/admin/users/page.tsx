import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { UsersFilterBar } from "@/components/admin/users-filter-bar";
import { InviteInstructorDialog } from "@/components/admin/invite-instructor-dialog";
import { PaginationControls } from "@/components/shared/pagination-controls";
import { searchUsers } from "@/lib/users/queries";
import type { UserRole } from "@/lib/api/types";

export const metadata = { title: "Users | Dhyan Mitra" };

const STATUS_VARIANT = {
  ACTIVE: "default",
  INACTIVE: "secondary",
  BLOCKED: "destructive",
} as const;

export default async function AdminUsersPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const page = Number(params.page ?? 0);
  const search = typeof params.search === "string" ? params.search : undefined;
  const role = typeof params.role === "string" ? (params.role as UserRole) : undefined;
  const status =
    typeof params.status === "string" ? (params.status as "ACTIVE" | "INACTIVE" | "BLOCKED") : undefined;

  const result = await searchUsers({ page, size: 20, sort: "createdAt,desc", search, role, status });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold tracking-tight">Users</h1>
        <InviteInstructorDialog />
      </div>

      <UsersFilterBar />

      <Card>
        <CardContent>
          {result.content.length === 0 ? (
            <p className="text-muted-foreground text-sm">No users match these filters.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Role</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {result.content.map((user) => (
                  <TableRow key={user.id}>
                    <TableCell className="font-medium">
                      <Link href={`/admin/users/${user.id}`} className="hover:underline">
                        {user.firstName} {user.lastName}
                      </Link>
                    </TableCell>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>{user.role}</TableCell>
                    <TableCell>
                      {user.status && <Badge variant={STATUS_VARIANT[user.status]}>{user.status}</Badge>}
                    </TableCell>
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
