import Link from "next/link";
import { redirect } from "next/navigation";
import { PlayCircle, ArrowRight, Quote } from "lucide-react";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { getSession } from "@/lib/auth/session";
import { HOME_FOR_ROLE } from "@/lib/auth/claims";
import { PublicShell } from "@/components/marketing/public-shell";
import { AutoplayVideo } from "@/components/marketing/autoplay-video";
import { CourseCard } from "@/components/courses/course-card";
import { listCourses } from "@/lib/courses/queries";
import { FEATURES, PROGRAMS, HIGHLIGHTS, TONE_BADGE, TONE_LINK } from "@/components/marketing/marketing-data";
import { SITE } from "@/lib/site";
import type { CourseSummary } from "@/lib/api/types";

async function getShowcaseCourses(): Promise<CourseSummary[]> {
  // Landing page must render even if the catalogue backend is unreachable, so
  // failures degrade to hiding the section rather than erroring the page.
  try {
    const result = await listCourses({ page: 0, size: 3, sort: "publishedAt,desc", status: "PUBLISHED" });
    return result.content;
  } catch {
    return [];
  }
}

export default async function Home() {
  const session = await getSession();
  if (session) {
    redirect(HOME_FOR_ROLE[session.role] ?? "/sign-in");
  }

  const courses = await getShowcaseCourses();

  return (
    <PublicShell>
      {/* Hero */}
      <section className="relative overflow-hidden">
        <video
          className="absolute inset-0 h-full w-full object-cover"
          src="/videos/video-1.mp4"
          autoPlay
          muted
          loop
          playsInline
          preload="metadata"
          aria-hidden="true"
        />
        <div className="from-background via-background/85 to-background/20 absolute inset-0 bg-gradient-to-r sm:to-transparent" />
        <div className="relative mx-auto max-w-6xl px-6 py-20 sm:py-28 lg:py-36">
          <div className="max-w-xl">
            <h1 className="text-4xl leading-tight font-bold tracking-tight text-balance sm:text-5xl lg:text-6xl">
              <span className="text-primary">Awaken Your</span>
              <br />
              <span className="text-(--brand-orange)">Inner Peace</span>
            </h1>
            <p className="text-foreground/80 mt-5 max-w-md text-lg">
              Yoga, Meditation, Pranayama &amp; Wellness for Body, Mind &amp; Soul.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link href="/register" className={cn(buttonVariants({ size: "lg" }))}>
                Start Your Journey
              </Link>
              <Link
                href="/gallery"
                className={cn(
                  buttonVariants({ size: "lg", variant: "outline" }),
                  "bg-background/70 gap-2 backdrop-blur",
                )}
              >
                <PlayCircle className="size-5" aria-hidden="true" />
                Watch Video
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Feature strip — overlaps the hero */}
      <div className="mx-auto -mt-10 max-w-6xl px-6">
        <div className="bg-card ring-border/60 relative z-10 grid grid-cols-2 gap-y-6 rounded-2xl px-6 py-6 shadow-lg ring-1 sm:grid-cols-3 lg:grid-cols-5">
          {FEATURES.map((feature) => (
            <div key={feature.title} className="flex flex-col items-center gap-2 px-2 text-center">
              <feature.icon className="text-primary size-6" aria-hidden="true" />
              <div>
                <p className="text-sm font-semibold">{feature.title}</p>
                <p className="text-muted-foreground text-xs">{feature.sub}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Welcome */}
      <section className="mx-auto grid max-w-6xl items-center gap-10 px-6 py-16 lg:grid-cols-2 lg:py-20">
        <AutoplayVideo src="/videos/video-2.mp4" label="Dhyan Mitra practice session" className="shadow-lg" />
        <div>
          <p className="text-(--brand-orange) text-sm font-semibold tracking-wide uppercase">
            Welcome to {SITE.name}
          </p>
          <h2 className="text-primary mt-3 text-3xl font-bold tracking-tight sm:text-4xl">
            Your Journey to Health, Happiness &amp; Harmony
          </h2>
          <p className="text-muted-foreground mt-5">
            At {SITE.name}, we believe in the power of yoga and meditation to transform lives. Our
            mission is to bring authentic yoga practices to everyone and help them lead a balanced,
            peaceful and healthy life — through recorded courses, live classes and personal guidance.
          </p>
          <Link href="/about" className={cn(buttonVariants(), "mt-7")}>
            Know More About Us
          </Link>
        </div>
      </section>

      {/* Programs */}
      <section className="bg-card/50 border-border/60 border-y">
        <div className="mx-auto max-w-6xl px-6 py-16 lg:py-20">
          <div className="mx-auto max-w-2xl text-center">
            <p className="text-(--brand-orange) text-sm font-semibold tracking-wide uppercase">
              Our Programs
            </p>
            <h2 className="text-primary mt-3 text-3xl font-bold tracking-tight sm:text-4xl">
              Programs for Every Need
            </h2>
          </div>
          <div className="mt-12 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {PROGRAMS.map((program) => (
              <div
                key={program.slug}
                className="bg-card ring-border/60 flex flex-col items-center rounded-xl p-6 text-center ring-1 transition-shadow hover:shadow-md"
              >
                <span className={cn("flex size-14 items-center justify-center rounded-full", TONE_BADGE[program.tone])}>
                  <program.icon className="size-7" aria-hidden="true" />
                </span>
                <h3 className="mt-4 font-semibold">{program.title}</h3>
                <p className="text-muted-foreground mt-2 text-sm">{program.desc}</p>
                <Link
                  href="/programs"
                  className={cn("mt-4 inline-flex items-center gap-1 text-sm font-medium", TONE_LINK[program.tone])}
                >
                  Explore <ArrowRight className="size-4" aria-hidden="true" />
                </Link>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Highlights band (honest — no invented numbers) */}
      <section className="bg-(--brand-green) text-white">
        <div className="mx-auto grid max-w-6xl gap-8 px-6 py-12 sm:grid-cols-2 lg:grid-cols-4">
          {HIGHLIGHTS.map((item) => (
            <div key={item.title} className="flex flex-col items-center gap-2 text-center">
              <item.icon className="size-8 text-white/90" aria-hidden="true" />
              <p className="text-lg font-semibold">{item.title}</p>
              <p className="text-sm text-white/75">{item.sub}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Course showcase (real published courses) */}
      {courses.length > 0 && (
        <section className="mx-auto max-w-6xl px-6 py-16 lg:py-20">
          <div className="flex flex-wrap items-end justify-between gap-4">
            <div className="max-w-2xl">
              <h2 className="text-primary text-3xl font-bold tracking-tight sm:text-4xl">
                Explore our courses
              </h2>
              <p className="text-muted-foreground mt-3">
                Structured programmes for every level, taught by experienced instructors.
              </p>
            </div>
            <Link href="/courses" className={cn(buttonVariants({ variant: "outline", size: "sm" }))}>
              View all courses
            </Link>
          </div>
          <div className="mt-10 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {courses.map((course) => (
              <CourseCard key={course.id} course={course} />
            ))}
          </div>
        </section>
      )}

      {/* Philosophy / message (honest replacement for fabricated testimonials) */}
      <section className="bg-card/50 border-border/60 border-y">
        <div className="mx-auto grid max-w-6xl items-center gap-10 px-6 py-16 lg:grid-cols-2 lg:py-20">
          <AutoplayVideo src="/videos/video-4.mp4" label="Yoga practice at Dhyan Mitra" className="shadow-lg" />
          <div>
            <p className="text-(--brand-orange) text-sm font-semibold tracking-wide uppercase">
              What We Believe
            </p>
            <Quote className="text-primary/30 mt-4 size-10" aria-hidden="true" />
            <blockquote className="mt-2 text-xl leading-relaxed font-medium text-balance">
              Yoga is not about touching your toes — it is about what you learn on the way down.
              Our purpose is to make authentic practice accessible to everyone, at every stage of life.
            </blockquote>
            <p className="text-muted-foreground mt-5 text-sm">
              — {SITE.proprietor}, {SITE.legalName}
            </p>
          </div>
        </div>
      </section>

      {/* Final CTA */}
      <section className="bg-(--brand-green-dark) text-white">
        <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-6 px-6 py-14 text-center sm:flex-row sm:text-left">
          <div>
            <h2 className="text-2xl font-bold tracking-tight sm:text-3xl">
              Begin Your Transformation Today
            </h2>
            <p className="mt-2 text-white/80">
              Join others on the path to better health, peace and fulfillment.
            </p>
          </div>
          <Link
            href="/register"
            className={cn(
              buttonVariants({ size: "lg" }),
              "bg-(--brand-orange-strong) hover:bg-(--brand-orange-strong)/90 shrink-0 gap-2 text-white",
            )}
          >
            Join Free Session Now <ArrowRight className="size-5" aria-hidden="true" />
          </Link>
        </div>
      </section>
    </PublicShell>
  );
}
