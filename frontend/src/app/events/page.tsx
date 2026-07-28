import Link from "next/link";
import { CalendarDays, Video, Users } from "lucide-react";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { PublicShell } from "@/components/marketing/public-shell";
import { SITE } from "@/lib/site";

export const metadata = {
  title: "Events | Dhyan Mitra",
  description: "Join Dhyan Mitra's live yoga classes, workshops and community sessions.",
};

const OFFERINGS = [
  {
    icon: Video,
    title: "Live Online Classes",
    desc: "Scheduled, instructor-led sessions you join over video to practise together in real time.",
  },
  {
    icon: CalendarDays,
    title: "Workshops & Retreats",
    desc: "Focused sessions on breathwork, meditation and wellness, announced to our members first.",
  },
  {
    icon: Users,
    title: "Community Sessions",
    desc: "Group practice and guided meditation to keep you motivated on your journey.",
  },
];

export default function EventsPage() {
  return (
    <PublicShell>
      <section className="border-border/60 bg-card/50 border-b">
        <div className="mx-auto max-w-3xl px-6 py-14 text-center">
          <p className="text-(--brand-orange) text-sm font-semibold tracking-wide uppercase">Events</p>
          <h1 className="text-primary mt-3 text-3xl font-bold tracking-tight sm:text-4xl">
            Live classes &amp; upcoming sessions
          </h1>
          <p className="text-muted-foreground mt-4">
            {SITE.name} runs live, instructor-led sessions throughout the week. Create a free account
            to see the current schedule and reserve your place — new sessions are added regularly.
          </p>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-6 py-16">
        <div className="grid gap-6 sm:grid-cols-3">
          {OFFERINGS.map((offering) => (
            <div key={offering.title} className="bg-card ring-border/60 rounded-xl p-6 ring-1">
              <offering.icon className="text-primary size-7" aria-hidden="true" />
              <h2 className="mt-4 font-semibold">{offering.title}</h2>
              <p className="text-muted-foreground mt-2 text-sm">{offering.desc}</p>
            </div>
          ))}
        </div>

        <div className="bg-(--brand-green-dark) mt-12 rounded-2xl px-6 py-10 text-center text-white">
          <h2 className="text-2xl font-bold tracking-tight">See what&apos;s on</h2>
          <p className="mx-auto mt-3 max-w-xl text-white/80">
            The live schedule lives inside your dashboard. Sign in or create a free account to view
            upcoming sessions and join.
          </p>
          <div className="mt-7 flex flex-wrap justify-center gap-3">
            <Link
              href="/register"
              className={cn(
                buttonVariants({ size: "lg" }),
                "bg-(--brand-orange-strong) hover:bg-(--brand-orange-strong)/90 text-white",
              )}
            >
              Create free account
            </Link>
            <Link
              href="/sign-in"
              className={cn(buttonVariants({ size: "lg", variant: "outline" }), "border-white/30 bg-white/10 text-white hover:bg-white/20")}
            >
              Sign in
            </Link>
          </div>
        </div>
      </section>
    </PublicShell>
  );
}
