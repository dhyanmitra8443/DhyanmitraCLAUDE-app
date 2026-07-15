"use client";

import Link from "next/link";
import { Bell } from "lucide-react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuSeparator, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { fetchOwnNotifications, markAllNotificationsRead, markNotificationRead } from "@/lib/notifications/client";

const NOTIFICATIONS_KEY = ["notifications"];

/** Ref: SRS 14.4-14.6 - shared across all three portals; polls for new notifications every 30s. */
export function NotificationBell({ inboxHref }: { inboxHref: string }) {
  const queryClient = useQueryClient();

  const recentQuery = useQuery({
    queryKey: [...NOTIFICATIONS_KEY, "recent"],
    queryFn: () => fetchOwnNotifications({ size: 8 }),
    refetchInterval: 30_000,
  });

  // No unread-count endpoint exists (Ref: lib/notifications/queries.ts) - a
  // second UNREAD-filtered query with size 1 reads the true total off
  // page.totalElements instead of relying on the small "recent" page above,
  // which would undercount once more than 8 notifications are unread.
  const unreadCountQuery = useQuery({
    queryKey: [...NOTIFICATIONS_KEY, "unread-count"],
    queryFn: () => fetchOwnNotifications({ readStatus: "UNREAD", size: 1 }),
    refetchInterval: 30_000,
  });

  function invalidate() {
    queryClient.invalidateQueries({ queryKey: NOTIFICATIONS_KEY });
  }

  const markReadMutation = useMutation({ mutationFn: markNotificationRead, onSuccess: invalidate });
  const markAllReadMutation = useMutation({ mutationFn: markAllNotificationsRead, onSuccess: invalidate });

  const notifications = recentQuery.data?.content ?? [];
  const unreadCount = unreadCountQuery.data?.page.totalElements ?? 0;

  return (
    <DropdownMenu>
      <DropdownMenuTrigger className="hover:bg-muted focus-visible:ring-ring/50 relative rounded-full p-2 outline-none focus-visible:ring-3">
        <Bell className="size-5" />
        {unreadCount > 0 && (
          <span className="bg-destructive text-destructive-foreground absolute -top-0.5 -right-0.5 flex size-4 items-center justify-center rounded-full text-[10px] font-medium">
            {unreadCount > 9 ? "9+" : unreadCount}
          </span>
        )}
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-80 p-0">
        <div className="flex items-center justify-between gap-2 px-3 py-2">
          <span className="text-sm font-medium">Notifications</span>
          {unreadCount > 0 && (
            <Button
              size="sm"
              variant="ghost"
              className="h-auto p-0 text-xs"
              disabled={markAllReadMutation.isPending}
              onClick={() => markAllReadMutation.mutate()}
            >
              Mark all read
            </Button>
          )}
        </div>
        <DropdownMenuSeparator className="my-0" />
        <div className="max-h-96 overflow-y-auto">
          {notifications.length === 0 ? (
            <p className="text-muted-foreground px-3 py-4 text-center text-sm">No notifications yet.</p>
          ) : (
            notifications.map((notification) => (
              <div
                key={notification.id}
                className={`flex items-start justify-between gap-2 border-b px-3 py-2 last:border-b-0 ${notification.readStatus === "UNREAD" ? "bg-accent/40" : ""}`}
              >
                <div className="space-y-0.5">
                  <p className="text-sm font-medium">{notification.title}</p>
                  <p className="text-muted-foreground text-xs">{notification.message}</p>
                  {notification.createdAt && (
                    <p className="text-muted-foreground text-[11px]">{new Date(notification.createdAt).toLocaleString()}</p>
                  )}
                </div>
                {notification.readStatus === "UNREAD" && notification.id && (
                  <Button
                    size="sm"
                    variant="ghost"
                    className="h-auto shrink-0 p-0 text-xs"
                    disabled={markReadMutation.isPending}
                    onClick={() => markReadMutation.mutate(notification.id!)}
                  >
                    Mark read
                  </Button>
                )}
              </div>
            ))
          )}
        </div>
        <DropdownMenuSeparator className="my-0" />
        <Link href={inboxHref} className="hover:bg-accent block px-3 py-2 text-center text-sm">
          View all
        </Link>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
