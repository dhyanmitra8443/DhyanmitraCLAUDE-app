import Link from "next/link";
import { redirect } from "next/navigation";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { getSession } from "@/lib/auth/session";
import { HOME_FOR_ROLE } from "@/lib/auth/claims";

export default async function Home() {
  const session = await getSession();
  if (session) {
    redirect(HOME_FOR_ROLE[session.role] ?? "/sign-in");
  }

  return (
    <div className="flex min-h-screen flex-col">
      <header className="flex items-center justify-between px-6 py-4 sm:px-10">
        <span className="text-lg font-semibold tracking-tight">Dhyan Mitra</span>
        <nav className="flex items-center gap-2">
          <Link href="/courses" className={cn(buttonVariants({ variant: "ghost" }))}>
            Courses
          </Link>
          <Link href="/sign-in" className={cn(buttonVariants({ variant: "ghost" }))}>
            Sign in
          </Link>
          <Link href="/register" className={cn(buttonVariants())}>
            Get started
          </Link>
        </nav>
      </header>

      <main className="flex flex-1 flex-col items-center justify-center px-6 text-center">
        <h1 className="max-w-2xl text-4xl font-semibold tracking-tight sm:text-5xl">
          Yoga courses, live classes, and certification — all in one place.
        </h1>
        <p className="text-muted-foreground mt-4 max-w-xl text-lg">
          Learn at your own pace with recorded courses, join live sessions with your instructor, and
          track your progress toward a certificate.
        </p>
        <div className="mt-8 flex gap-3">
          <Link href="/register" className={cn(buttonVariants({ size: "lg" }))}>
            Create your account
          </Link>
          <Link href="/sign-in" className={cn(buttonVariants({ size: "lg", variant: "outline" }))}>
            Sign in
          </Link>
        </div>
      </main>

      <footer className="text-muted-foreground px-6 py-6 text-center text-sm">
        © {new Date().getFullYear()} Dhyan Mitra
      </footer>
    </div>
  );
}
