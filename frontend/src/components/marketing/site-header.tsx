import Link from "next/link";
import Image from "next/image";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

// Shared header for public/marketing pages: brand badge + wordmark, then the
// standard visitor nav. The logo art is a circular badge on a black square
// canvas, so `rounded-full` clips the corners to show it as intended.
export function SiteHeader() {
  return (
    <header className="border-border/60 bg-background/80 sticky top-0 z-40 border-b backdrop-blur">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-3">
        <Link href="/" className="flex items-center gap-3">
          <Image
            src="/brand/dhyan-mitra-logo.jpg"
            alt="Dhyan Mitra logo"
            width={44}
            height={44}
            priority
            className="border-primary/30 h-11 w-11 rounded-full border object-cover"
          />
          <span className="text-lg leading-tight font-semibold tracking-tight">Dhyan Mitra</span>
        </Link>

        <nav className="flex items-center gap-1 sm:gap-2">
          <Link
            href="/courses"
            className={cn(buttonVariants({ variant: "ghost", size: "sm" }), "hidden sm:inline-flex")}
          >
            Courses
          </Link>
          <Link href="/sign-in" className={cn(buttonVariants({ variant: "ghost", size: "sm" }))}>
            Sign in
          </Link>
          <Link href="/register" className={cn(buttonVariants({ size: "sm" }))}>
            Get started
          </Link>
        </nav>
      </div>
    </header>
  );
}
