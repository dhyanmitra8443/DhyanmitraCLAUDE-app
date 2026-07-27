"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState, type ComponentType } from "react";
import {
  Award,
  BarChart3,
  Bell,
  BookOpen,
  Circle,
  CreditCard,
  FolderTree,
  Gift,
  LayoutDashboard,
  MoreHorizontal,
  Receipt,
  Repeat,
  ScrollText,
  Settings,
  Share2,
  User,
  Users,
  Video,
  Wallet,
  X,
} from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { cn } from "@/lib/utils";

export interface AppNavItem {
  href: string;
  label: string;
  /** Key into ICONS below; falls back to a neutral dot when unknown/absent. */
  icon?: string;
  /** Surfaced in the always-visible bar (desktop inline row, mobile bottom tabs). */
  primary?: boolean;
}

type IconType = ComponentType<{ className?: string }>;

// Named string keys (not component refs) so nav config can live in server
// layout files and cross the server→client boundary as plain data.
const ICONS: Record<string, IconType> = {
  dashboard: LayoutDashboard,
  courses: BookOpen,
  live: Video,
  certificates: Award,
  subscriptions: Repeat,
  payments: Wallet,
  orders: Receipt,
  refer: Gift,
  referrals: Share2,
  reports: BarChart3,
  notifications: Bell,
  "notification-log": ScrollText,
  profile: User,
  users: Users,
  categories: FolderTree,
  settings: Settings,
  billing: CreditCard,
};

function iconFor(key?: string): IconType {
  return (key && ICONS[key]) || Circle;
}

/**
 * Active when the path is the item itself or nested beneath it. The area root
 * (first nav item, e.g. /learn) matches exactly only, so it doesn't light up
 * for every child route.
 */
function useIsActive(rootHref: string) {
  const pathname = usePathname();
  return (href: string) => {
    if (href === rootHref) return pathname === href;
    return pathname === href || pathname.startsWith(`${href}/`);
  };
}

export function AppNav({ navItems }: { navItems: AppNavItem[] }) {
  const rootHref = navItems[0]?.href ?? "/";
  const isActive = useIsActive(rootHref);

  const primaryItems = navItems.filter((item) => item.primary);
  const secondaryItems = navItems.filter((item) => !item.primary);

  const [drawerOpen, setDrawerOpen] = useState(false);

  // Lock background scroll while the mobile "More" drawer is open.
  useEffect(() => {
    if (!drawerOpen) return;
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = previous;
    };
  }, [drawerOpen]);

  return (
    <>
      {/* Desktop: primary links inline + a "More" dropdown for the rest. */}
      <nav className="hidden items-center gap-1 md:flex">
        {primaryItems.map((item) => {
          const Icon = iconFor(item.icon);
          const active = isActive(item.href);
          return (
            <Link
              key={item.href}
              href={item.href}
              aria-current={active ? "page" : undefined}
              className={cn(
                "flex items-center gap-1.5 rounded-md px-2.5 py-1.5 text-sm transition-colors",
                active
                  ? "bg-muted text-foreground font-medium"
                  : "text-muted-foreground hover:bg-muted hover:text-foreground",
              )}
            >
              <Icon className="size-4" />
              {item.label}
            </Link>
          );
        })}

        {secondaryItems.length > 0 && (
          <DropdownMenu>
            <DropdownMenuTrigger
              className={cn(
                "flex items-center gap-1.5 rounded-md px-2.5 py-1.5 text-sm outline-none transition-colors",
                secondaryItems.some((item) => isActive(item.href))
                  ? "bg-muted text-foreground font-medium"
                  : "text-muted-foreground hover:bg-muted hover:text-foreground",
              )}
            >
              <MoreHorizontal className="size-4" />
              More
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-52">
              {secondaryItems.map((item) => {
                const Icon = iconFor(item.icon);
                return (
                  <DropdownMenuItem key={item.href} render={<Link href={item.href} />}>
                    <Icon className="size-4" />
                    {item.label}
                  </DropdownMenuItem>
                );
              })}
            </DropdownMenuContent>
          </DropdownMenu>
        )}
      </nav>

      {/* Mobile: fixed bottom tab bar with primary items + a "More" button. */}
      <nav className="bg-background fixed inset-x-0 bottom-0 z-40 flex items-stretch border-t md:hidden">
        {primaryItems.map((item) => {
          const Icon = iconFor(item.icon);
          const active = isActive(item.href);
          return (
            <Link
              key={item.href}
              href={item.href}
              aria-current={active ? "page" : undefined}
              className={cn(
                "flex flex-1 flex-col items-center justify-center gap-0.5 py-2 text-[11px]",
                active ? "text-foreground font-medium" : "text-muted-foreground",
              )}
            >
              <Icon className="size-5" />
              <span className="max-w-full truncate px-0.5">{item.label}</span>
            </Link>
          );
        })}
        {secondaryItems.length > 0 && (
          <button
            type="button"
            onClick={() => setDrawerOpen(true)}
            aria-label="More options"
            aria-expanded={drawerOpen}
            className={cn(
              "flex flex-1 flex-col items-center justify-center gap-0.5 py-2 text-[11px]",
              drawerOpen ? "text-foreground font-medium" : "text-muted-foreground",
            )}
          >
            <MoreHorizontal className="size-5" />
            <span>More</span>
          </button>
        )}
      </nav>

      {/* Mobile "More" drawer: slide-up sheet listing the remaining features. */}
      {drawerOpen && (
        <div className="fixed inset-0 z-50 md:hidden" role="dialog" aria-modal="true">
          <div
            className="absolute inset-0 bg-black/50"
            onClick={() => setDrawerOpen(false)}
          />
          <div className="bg-background absolute inset-x-0 bottom-0 max-h-[80vh] overflow-y-auto rounded-t-2xl border-t p-4 pb-6 shadow-lg">
            <div className="mb-3 flex items-center justify-between">
              <span className="text-sm font-semibold">More</span>
              <button
                type="button"
                onClick={() => setDrawerOpen(false)}
                aria-label="Close"
                className="hover:bg-muted rounded-full p-1.5"
              >
                <X className="size-5" />
              </button>
            </div>
            <div className="grid grid-cols-3 gap-2">
              {secondaryItems.map((item) => {
                const Icon = iconFor(item.icon);
                const active = isActive(item.href);
                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    onClick={() => setDrawerOpen(false)}
                    aria-current={active ? "page" : undefined}
                    className={cn(
                      "flex flex-col items-center gap-1.5 rounded-lg border p-3 text-center text-xs",
                      active
                        ? "border-foreground/20 bg-muted text-foreground font-medium"
                        : "text-muted-foreground hover:bg-muted",
                    )}
                  >
                    <Icon className="size-5" />
                    <span className="leading-tight">{item.label}</span>
                  </Link>
                );
              })}
            </div>
          </div>
        </div>
      )}
    </>
  );
}
