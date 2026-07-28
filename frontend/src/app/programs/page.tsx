import Link from "next/link";
import { ArrowRight } from "lucide-react";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { PublicShell } from "@/components/marketing/public-shell";
import { PROGRAMS, TONE_BADGE } from "@/components/marketing/marketing-data";

export const metadata = {
  title: "Programs | Dhyan Mitra",
  description:
    "Explore Dhyan Mitra's yoga, pranayama, meditation, wellness, spiritual growth and retreat programs.",
};

export default function ProgramsPage() {
  return (
    <PublicShell>
      <section className="border-border/60 bg-card/50 border-b">
        <div className="mx-auto max-w-3xl px-6 py-14 text-center">
          <p className="text-(--brand-orange) text-sm font-semibold tracking-wide uppercase">
            Our Programs
          </p>
          <h1 className="text-primary mt-3 text-3xl font-bold tracking-tight sm:text-4xl">
            Programs for Every Need
          </h1>
          <p className="text-muted-foreground mt-4">
            Whatever brought you to the mat, there is a practice for it — guided by experienced,
            certified teachers.
          </p>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-6 py-16">
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {PROGRAMS.map((program) => (
            <div
              key={program.slug}
              id={program.slug}
              className="bg-card ring-border/60 flex flex-col rounded-xl p-6 ring-1 transition-shadow hover:shadow-md"
            >
              <span className={cn("flex size-14 items-center justify-center rounded-full", TONE_BADGE[program.tone])}>
                <program.icon className="size-7" aria-hidden="true" />
              </span>
              <h2 className="mt-4 text-lg font-semibold">{program.title}</h2>
              <p className="text-muted-foreground mt-2 text-sm">{program.desc}</p>
              <Link
                href="/courses"
                className={cn(buttonVariants({ variant: "outline", size: "sm" }), "mt-5 w-fit gap-1")}
              >
                Find courses <ArrowRight className="size-4" aria-hidden="true" />
              </Link>
            </div>
          ))}
        </div>
      </section>

      <section className="bg-(--brand-green-dark) text-white">
        <div className="mx-auto max-w-4xl px-6 py-14 text-center">
          <h2 className="text-2xl font-bold tracking-tight sm:text-3xl">
            Not sure where to start?
          </h2>
          <p className="mx-auto mt-3 max-w-xl text-white/80">
            Create a free account and we&apos;ll help you find the right practice for your goals and
            experience level.
          </p>
          <Link
            href="/register"
            className={cn(
              buttonVariants({ size: "lg" }),
              "bg-(--brand-orange-strong) hover:bg-(--brand-orange-strong)/90 mt-7 text-white",
            )}
          >
            Join Free Session
          </Link>
        </div>
      </section>
    </PublicShell>
  );
}
