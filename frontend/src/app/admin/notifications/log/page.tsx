import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { NotificationsLogFilterBar } from "@/components/notifications/notifications-log-filter-bar";
import { PaginationControls } from "@/components/shared/pagination-controls";
import { searchNotifications } from "@/lib/notifications/queries";
import type { NotificationDeliveryChannel } from "@/lib/api/types";

export const metadata = { title: "Notification log | Dhyan Mitra" };

const READ_STATUS_VARIANT = {
  UNREAD: "outline",
  READ: "secondary",
} as const;

/** Ref: SRS 14.13 - administrator-only system-wide notification log. */
export default async function AdminNotificationLogPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const page = Number(params.page ?? 0);
  const userId = typeof params.userId === "string" ? params.userId : undefined;
  const notificationType = typeof params.notificationType === "string" ? params.notificationType : undefined;
  const deliveryChannel =
    typeof params.deliveryChannel === "string" ? (params.deliveryChannel as NotificationDeliveryChannel) : undefined;

  const result = await searchNotifications({ page, size: 20, userId, notificationType, deliveryChannel });

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Notification log</h1>

      <NotificationsLogFilterBar />

      <Card>
        <CardContent>
          {result.content.length === 0 ? (
            <p className="text-muted-foreground text-sm">No notifications match these filters.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Recipient</TableHead>
                  <TableHead>Title</TableHead>
                  <TableHead>Channel</TableHead>
                  <TableHead>Module</TableHead>
                  <TableHead>Sent</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {result.content.map((notification) => (
                  <TableRow key={notification.id}>
                    <TableCell className="text-muted-foreground font-mono text-xs">{notification.recipientUserId}</TableCell>
                    <TableCell className="font-medium">{notification.title}</TableCell>
                    <TableCell>{notification.notificationType}</TableCell>
                    <TableCell>{notification.relatedModule ?? "—"}</TableCell>
                    <TableCell>{notification.createdAt ? new Date(notification.createdAt).toLocaleString() : "—"}</TableCell>
                    <TableCell>
                      {notification.readStatus && (
                        <Badge variant={READ_STATUS_VARIANT[notification.readStatus]}>{notification.readStatus}</Badge>
                      )}
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
