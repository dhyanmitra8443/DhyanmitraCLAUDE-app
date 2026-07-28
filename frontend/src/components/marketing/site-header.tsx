"use client";

import Link from "next/link";
import Image from "next/image";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import { Mail, Phone, Menu, X } from "lucide-react";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { SITE, activeSocials } from "@/lib/site";
import { SOCIAL_ICONS } from "./social-icons";

const NAV = [
  { href: "/", label: "Home" },
  { href: "/about", label: "About Us" },
  { href: "/courses", label: "Courses" },
  { href: "/programs", label: "Programs" },
  { href: "/blog", label: "Blog" },
  { href: "/events", label: "Events" },
  { href: "/gallery", label: "Gallery" },
  { href: "/contact", label: "Contact" },
];

export function SiteHeader() {
  const pathname = usePathname();
  const [open, setOpen] = useState(false);
  const socials = activeSocials();

  // Close the mobile menu on route change and lock body scroll while open.
  useEffect(() => {
    setOpen(false);
  }, [pathname]);

  useEffect(() => {
    if (!open) return;
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = previous;
    };
  }, [open]);

  const isActive = (href: string) => (href === "/" ? pathname === "/" : pathname.startsWith(href));

  return (
    <header className="sticky top-0 z-40">
      {/* Top utility bar */}
      <div className="bg-(--brand-green-dark) text-white/90">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-x-6 gap-y-1 px-4 py-1.5 text-xs sm:px-6">
          <div className="flex flex-wrap items-center gap-x-5 gap-y-1">
            <a href={`mailto:${SITE.email}`} className="inline-flex items-center gap-1.5 hover:text-white">
              <Mail className="size-3.5" aria-hidden="true" />
              {SITE.email}
            </a>
            <a href={`tel:${SITE.phone.replace(/\s+/g, "")}`} className="inline-flex items-center gap-1.5 hover:text-white">
              <Phone className="size-3.5" aria-hidden="true" />
              {SITE.phone}
            </a>
          </div>
          <div className="flex items-center gap-3">
            {socials.length > 0 && (
              <div className="hidden items-center gap-2 sm:flex">
                <span className="text-white/70">Follow us:</span>
                {socials.map(([key, url]) => {
                  const Icon = SOCIAL_ICONS[key];
                  return (
                    <a
                      key={key}
                      href={url}
                      target="_blank"
                      rel="noopener noreferrer"
                      aria-label={key}
                      className="text-white/80 hover:text-white"
                    >
                      <Icon className="size-3.5" />
                    </a>
                  );
                })}
              </div>
            )}
            <Link href="/sign-in" className="font-medium hover:text-white">
              Sign in
            </Link>
          </div>
        </div>
      </div>

      {/* Main bar */}
      <div className="border-border/60 bg-background/90 border-b backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3 sm:px-6">
          <Link href="/" className="flex items-center gap-2.5">
            <Image
              src="/brand/dhyan-mitra-logo.jpg"
              alt="Dhyan Mitra logo"
              width={44}
              height={44}
              priority
              className="border-primary/30 h-11 w-11 rounded-full border object-cover"
            />
            <span className="leading-tight">
              <span className="block text-lg font-semibold tracking-tight">
                {SITE.name}
                <span className="text-(--brand-orange)">.in</span>
              </span>
              <span className="text-muted-foreground block text-[11px]">{SITE.tagline}</span>
            </span>
          </Link>

          <nav className="hidden items-center gap-0.5 lg:flex">
            {NAV.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                aria-current={isActive(item.href) ? "page" : undefined}
                className={cn(
                  "rounded-md px-2.5 py-1.5 text-sm transition-colors",
                  isActive(item.href)
                    ? "text-primary font-semibold"
                    : "text-foreground/80 hover:text-primary",
                )}
              >
                {item.label}
              </Link>
            ))}
          </nav>

          <div className="flex items-center gap-2">
            <Link
              href="/register"
              className={cn(
                buttonVariants({ size: "sm" }),
                "bg-(--brand-orange-strong) hover:bg-(--brand-orange-strong)/90 hidden text-white sm:inline-flex",
              )}
            >
              Join Free Session
            </Link>
            <button
              type="button"
              onClick={() => setOpen((v) => !v)}
              aria-label={open ? "Close menu" : "Open menu"}
              aria-expanded={open}
              className="hover:bg-muted rounded-md p-2 lg:hidden"
            >
              {open ? <X className="size-5" /> : <Menu className="size-5" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile menu */}
      {open && (
        <div className="border-border/60 bg-background border-b shadow-sm lg:hidden">
          <nav className="mx-auto flex max-w-6xl flex-col px-4 py-2 sm:px-6">
            {NAV.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                aria-current={isActive(item.href) ? "page" : undefined}
                className={cn(
                  "rounded-md px-2 py-2.5 text-sm",
                  isActive(item.href) ? "text-primary font-semibold" : "text-foreground/80",
                )}
              >
                {item.label}
              </Link>
            ))}
            <Link
              href="/register"
              className={cn(
                buttonVariants({ size: "sm" }),
                "bg-(--brand-orange-strong) hover:bg-(--brand-orange-strong)/90 mt-2 mb-2 text-white",
              )}
            >
              Join Free Session
            </Link>
          </nav>
        </div>
      )}
    </header>
  );
}
