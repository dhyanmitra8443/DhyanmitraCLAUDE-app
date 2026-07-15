import { Card, CardContent } from "@/components/ui/card";
import { NotificationsFilterBar } from "@/components/notifications/notifications-filter-bar";
import { NotificationsList } from "@/components/notifications/notifications-list";
import { MarkAllReadButton } from "@/components/notifications/mark-all-read-button";
import { PaginationControls } from "@/components/shared/pagination-controls";
import { getOwnNotifications } from "@/lib/notifications/queries";
import type { NotificationReadStatus } from "@/lib/api/types";

export const metadata = { title: "Notifications | Dhyan Mitra" };

export default async function StudentNotificationsPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const page = Number(params.page ?? 0);
  const readStatus = typeof params.readStatus === "string" ? (params.readStatus as NotificationReadStatus) : undefined;

  const result = await getOwnNotifications({ page, size: 20, readStatus });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold tracking-tight">Notifications</h1>
        <MarkAllReadButton />
      </div>

      <NotificationsFilterBar basePath="/learn/notifications" />

      <Card>
        <CardContent>
          <NotificationsList notifications={result.content} />
        </CardContent>
      </Card>

      <PaginationControls page={result.page.page ?? 0} totalPages={result.page.totalPages ?? 1} />
    </div>
  );
}
