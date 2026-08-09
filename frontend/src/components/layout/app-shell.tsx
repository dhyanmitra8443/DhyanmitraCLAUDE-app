import Link from "next/link";
import type { ReactNode } from "react";
import { UserMenu } from "./user-menu";
import { AppNav, type AppNavItem } from "./app-nav";
import { ThemeToggle } from "./theme-toggle";
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
    // `theme-brand` matches the public site's palette (green/orange on cream)
    // so signed-in areas share one visual identity with the marketing pages.
    <div className="theme-brand bg-background text-foreground flex min-h-screen flex-col">
      <header className="flex items-center justify-between gap-3 border-b px-4 py-3 sm:px-6">
        <div className="flex min-w-0 items-center gap-3 md:gap-6">
          <Link href="/" className="text-base font-semibold tracking-tight whitespace-nowrap sm:text-lg">
            Dhyan Mitra
          </Link>
          {/*
          The signed-in user's designation (Student / Instructor /
          Administrator). Shown at every breakpoint: on mobile the nav collapses
          to a bottom tab bar, so this badge is the only thing left in the
          header telling the user which area they are in.
        */}
        <span className="bg-primary/10 text-primary shrink-0 rounded-full px-2 py-0.5 text-[11px] font-semibold whitespace-nowrap sm:px-2.5 sm:text-xs">
          {areaLabel}
        </span>
          <AppNav navItems={navItems} />
        </div>

        <div className="flex shrink-0 items-center gap-2">
          {/* Signed-in only: AppShell never renders for logged-out visitors. */}
          <ThemeToggle />
          <NotificationBell inboxHref={notificationsHref} />
          <UserMenu firstName={user.firstName} lastName={user.lastName} email={user.email} />
        </div>
      </header>

      {/* Bottom padding on mobile clears the fixed bottom tab bar (AppNav). */}
      <main className="flex-1 px-4 py-6 pb-24 sm:px-6 md:py-8 md:pb-8">{children}</main>
    </div>
  );
}
