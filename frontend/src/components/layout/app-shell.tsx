import Link from "next/link";
import type { ReactNode } from "react";
import { UserMenu } from "./user-menu";
import { AppNav, type AppNavItem } from "./app-nav";
import { NotificationBell } from "@/components/notifications/notification-bell";

export type AppShellNavItem = AppNavItem;

export function AppShell({
  areaLabel,
  navItems,
  notificationsHref,
  user,
  children,
}: {
  areaLabel: string;
  navItems: AppShellNavItem[];
  notificationsHref: string;
  user: { firstName: string; lastName: string; email: string };
  children: ReactNode;
}) {
  return (
    <div className="flex min-h-screen flex-col">
      <header className="flex items-center justify-between gap-3 border-b px-4 py-3 sm:px-6">
        <div className="flex min-w-0 items-center gap-3 md:gap-6">
          <Link href="/" className="text-base font-semibold tracking-tight whitespace-nowrap sm:text-lg">
            Dhyan Mitra
          </Link>
          <span className="text-muted-foreground hidden text-sm sm:inline">{areaLabel}</span>
          <AppNav navItems={navItems} />
        </div>

        <div className="flex shrink-0 items-center gap-2">
          <NotificationBell inboxHref={notificationsHref} />
          <UserMenu firstName={user.firstName} lastName={user.lastName} email={user.email} />
        </div>
      </header>

      {/* Bottom padding on mobile clears the fixed bottom tab bar (AppNav). */}
      <main className="flex-1 px-4 py-6 pb-24 sm:px-6 md:py-8 md:pb-8">{children}</main>
    </div>
  );
}
