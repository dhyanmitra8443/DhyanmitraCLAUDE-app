import { Badge } from "@/components/ui/badge";
import { MarkReadButton } from "./mark-read-button";
import type { NotificationSummary } from "@/lib/api/types";

export function NotificationsList({ notifications }: { notifications: NotificationSummary[] }) {
  if (notifications.length === 0) {
    return <p className="text-muted-foreground text-sm">No notifications.</p>;
  }

  return (
    <ul className="space-y-2">
      {notifications.map((notification) => (
        <li
          key={notification.id}
          className={`flex items-start justify-between gap-4 rounded-lg border px-3 py-2 ${notification.readStatus === "UNREAD" ? "bg-accent/40" : ""}`}
        >
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <p className="text-sm font-medium">{notification.title}</p>
              {notification.readStatus === "UNREAD" && <Badge variant="default">New</Badge>}
            </div>
            <p className="text-muted-foreground text-sm">{notification.message}</p>
            {notification.createdAt && (
              <p className="text-muted-foreground text-xs">{new Date(notification.createdAt).toLocaleString()}</p>
            )}
          </div>
          {notification.readStatus === "UNREAD" && notification.id && (
            <MarkReadButton notificationId={notification.id} />
          )}
        </li>
      ))}
    </ul>
  );
}
