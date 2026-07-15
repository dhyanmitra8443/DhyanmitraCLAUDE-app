import Link from "next/link";
import type { ReactNode } from "react";
import { UserMenu } from "./user-menu";
import { NotificationBell } from "@/components/notifications/notification-bell";

export interface AppShellNavItem {
  href: string;
  label: string;
}

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
      <header className="flex items-center justify-between border-b px-6 py-3">
        <div className="flex items-center gap-6">
          <Link href="/" className="text-lg font-semibold tracking-tight">
            Dhyan Mitra
          </Link>
          <span className="text-muted-foreground text-sm">{areaLabel}</span>
          <nav className="flex items-center gap-1">
            {navItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className="text-muted-foreground hover:bg-muted hover:text-foreground rounded-md px-2.5 py-1.5 text-sm"
              >
                {item.label}
              </Link>
            ))}
          </nav>
        </div>

        <div className="flex items-center gap-2">
          <NotificationBell inboxHref={notificationsHref} />
          <UserMenu firstName={user.firstName} lastName={user.lastName} email={user.email} />
        </div>
      </header>

      <main className="flex-1 px-6 py-8">{children}</main>
    </div>
  );
}
