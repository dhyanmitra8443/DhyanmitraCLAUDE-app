import Link from "next/link";
import Image from "next/image";
import { redirect } from "next/navigation";
import { Check } from "lucide-react";
import { buttonVariants } from "@/components/ui/button";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import { cn } from "@/lib/utils";
import { getSession } from "@/lib/auth/session";
import { HOME_FOR_ROLE } from "@/lib/auth/claims";
import { PublicShell } from "@/components/marketing/public-shell";
import { AutoplayVideo } from "@/components/marketing/autoplay-video";
import { CourseCard } from "@/components/courses/course-card";
import { listCourses } from "@/lib/courses/queries";
import type { CourseSummary } from "@/lib/api/types";

// Additional practice clips shown in the gallery, after the hero (video-1).
const GALLERY_VIDEOS = [
  { src: "/videos/video-2.mp4", label: "Practice session 2" },
  { src: "/videos/video-3.mp4", label: "Practice session 3" },
  { src: "/videos/video-4.mp4", label: "Practice session 4" },
  { src: "/videos/video-5.mp4", label: "Practice session 5" },
  { src: "/videos/video-6.mp4", label: "Practice session 6" },
  { src: "/videos/video-7.mp4", label: "Practice session 7" },
];

// "For Your Unique Needs" cards. Images are license-free Pexels photos stored
// locally in public/images/needs/ (see the alt text for each subject).
const NEEDS = [
  {
    img: "/images/needs/flexibility.jpg",
    alt: "Woman practicing online yoga for flexibility and strength building",
    title: "Yoga for Flexibility & Strength",
    desc: "Build lean muscle and improve mobility with targeted sequences.",
  },
  {
    img: "/images/needs/pain-relief.jpg",
    alt: "Personalized yoga poses for back pain relief and joint health",
    title: "Yoga for Pain Relief",
    desc: "Ease back pain, neck tension, and joint stiffness naturally.",
  },
  {
    img: "/images/needs/meditation.jpg",
    alt: "Online yoga meditation and breathing exercises for stress relief",
    title: "Yoga for Stress, Anxiety & Meditation",
    desc: "Find calm through guided meditation, breathwork, and mindful movement.",
  },
  {
    img: "/images/needs/prenatal.jpg",
    alt: "Safe prenatal yoga classes for expecting mothers",
    title: "Prenatal, Fertility & Postnatal Yoga",
    desc: "Safe, nurturing practice for expecting mothers and postpartum recovery.",
  },
  {
    img: "/images/needs/weight-loss.jpg",
    alt: "Active online yoga flow for weight loss and metabolism",
    title: "Yoga for Weight Loss",
    desc: "Support weight loss with mindful movement, stress relief, and improved metabolism.",
  },
  {
    img: "/images/needs/seniors.jpg",
    alt: "Gentle online yoga classes for seniors — balance and mobility",
    title: "Yoga for Seniors",
    desc: "Maintain mobility, balance, and vitality at every life stage.",
  },
];

const ONE_ON_ONE_POINTS = [
  "No crowd, no judgment — just you and your teacher",
  "Gentle, guided sessions built just for you",
  "Ask questions freely — it's your time",
  "Connecting live over Zoom",
];

const FAQ = [
  {
    q: "How do the online yoga courses work?",
    a: "Each course is a structured set of recorded lessons you can follow at your own pace, on any device. Enroll, then work through sections and lessons whenever it suits you — your progress is saved automatically.",
  },
  {
    q: "What is the difference between recorded courses and live classes?",
    a: "Recorded courses are always available for self-paced learning. Live classes are scheduled instructor-led sessions you join at a set time to practise together and ask questions in real time.",
  },
  {
    q: "Do I get a certificate?",
    a: "Yes. When you complete all the requirements of an eligible course, Dhyan Mitra issues a verifiable certificate of completion that you can download and share.",
  },
  {
    q: "Do I need any experience to start?",
    a: "No. Courses are labelled by difficulty — Beginner, Intermediate and Advanced — so you can start exactly where you are and progress from there.",
  },
  {
    q: "What do I need to practise at home?",
    a: "Just a stable internet connection, a little quiet space and a yoga mat. Wear comfortable clothing and keep water nearby.",
  },
];

async function getShowcaseCourses(): Promise<CourseSummary[]> {
  // Landing page must render even if the catalogue backend is unreachable, so
  // failures degrade to hiding the section rather than erroring the whole page.
  try {
    const result = await listCourses({
      page: 0,
      size: 3,
      sort: "publishedAt,desc",
      status: "PUBLISHED",
    });
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
      <section className="mx-auto grid max-w-6xl items-center gap-10 px-6 py-14 lg:grid-cols-2 lg:py-20">
        <div>
          <span className="border-primary/30 bg-primary/10 text-primary inline-flex items-center rounded-full border px-3 py-1 text-xs font-medium">
            स्वस्थ भारत · ध्यान · योग · जागरण
          </span>
          <h1 className="mt-5 text-4xl font-semibold tracking-tight text-balance sm:text-5xl">
            Yoga courses, live classes, and certification — all in one place.
          </h1>
          <p className="text-muted-foreground mt-4 max-w-xl text-lg">
            Learn at your own pace with recorded courses, join live sessions with your instructor,
            and earn a verifiable certificate as you progress.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Link href="/register" className={cn(buttonVariants({ size: "lg" }))}>
              Create your account
            </Link>
            <Link
              href="/courses"
              className={cn(buttonVariants({ size: "lg", variant: "outline" }))}
            >
              Browse courses
            </Link>
          </div>
        </div>

        <AutoplayVideo src="/videos/video-1.mp4" label="Introduction video" className="shadow-xl" />
      </section>

      {/* Video gallery */}
      <section className="bg-card/40 border-border/60 border-y">
        <div className="mx-auto max-w-6xl px-6 py-14">
          <div className="max-w-2xl">
            <h2 className="text-2xl font-semibold tracking-tight sm:text-3xl">
              See the practice in action
            </h2>
            <p className="text-muted-foreground mt-3">
              A glimpse of the sessions and techniques you&apos;ll learn with Dhyan Mitra. Tap the
              speaker icon on any clip to hear it.
            </p>
          </div>
          <div className="mt-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {GALLERY_VIDEOS.map((video) => (
              <AutoplayVideo key={video.src} src={video.src} label={video.label} />
            ))}
          </div>
        </div>
      </section>

      {/* Course showcase (real published courses) */}
      {courses.length > 0 && (
        <section className="mx-auto max-w-6xl px-6 py-14">
          <div className="flex flex-wrap items-end justify-between gap-4">
            <div className="max-w-2xl">
              <h2 className="text-2xl font-semibold tracking-tight sm:text-3xl">Explore courses</h2>
              <p className="text-muted-foreground mt-3">
                Structured programmes for every level, taught by experienced instructors.
              </p>
            </div>
            <Link href="/courses" className={cn(buttonVariants({ variant: "outline", size: "sm" }))}>
              View all courses
            </Link>
          </div>
          <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {courses.map((course) => (
              <CourseCard key={course.id} course={course} />
            ))}
          </div>
        </section>
      )}

      {/* Certification showcase */}
      <section className="bg-card/40 border-border/60 border-y">
        <div className="mx-auto max-w-3xl px-6 py-14 text-center">
          <h2 className="text-2xl font-semibold tracking-tight sm:text-3xl">
            Earn a certificate you can share
          </h2>
          <p className="text-muted-foreground mx-auto mt-4 max-w-xl">
            Complete an eligible course and receive a verifiable DYJK Dhyan Mitra certificate of
            completion — recognising your dedication to the practice and principles of yoga.
          </p>
          <div className="mt-6">
            <Link href="/courses" className={cn(buttonVariants())}>
              Start a course
            </Link>
          </div>
        </div>
      </section>

      {/* For Your Unique Needs */}
      <section className="mx-auto max-w-6xl px-6 py-14">
        <div className="mx-auto max-w-2xl text-center">
          <h2 className="text-2xl font-semibold tracking-tight sm:text-3xl">For Your Unique Needs</h2>
          <p className="text-muted-foreground mt-3">
            Whatever brought you to the mat, there&apos;s a practice for it — guided by experienced
            teachers.
          </p>
        </div>

        <div className="mt-10 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {NEEDS.map((need) => (
            <div
              key={need.title}
              className="bg-card ring-border/60 overflow-hidden rounded-xl ring-1 transition-shadow hover:shadow-md"
            >
              <div className="relative aspect-[3/2] w-full overflow-hidden">
                <Image
                  src={need.img}
                  alt={need.alt}
                  fill
                  sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 33vw"
                  className="object-cover"
                />
              </div>
              <div className="p-5">
                <h3 className="font-semibold">{need.title}</h3>
                <p className="text-muted-foreground mt-2 text-sm">{need.desc}</p>
              </div>
            </div>
          ))}
        </div>

        <div className="mt-10 flex flex-col items-center gap-1">
          <Link href="/register" className={cn(buttonVariants({ size: "lg" }))}>
            Book a free session
          </Link>
          <span className="text-muted-foreground text-xs">No credit card required</span>
        </div>
      </section>

      {/* 1-on-1 Yoga Sessions */}
      <section className="bg-card/40 border-border/60 border-y">
        <div className="mx-auto grid max-w-6xl items-center gap-10 px-6 py-14 lg:grid-cols-2">
          <div>
            <h2 className="text-2xl font-semibold tracking-tight sm:text-3xl">
              1-on-1 Yoga Sessions
            </h2>
            <p className="text-muted-foreground mt-4">
              Our 1-on-1 sessions aren&apos;t about judgment — they&apos;re about support. Your
              teacher meets you where you are, helps you move safely, and celebrates every small win.
            </p>
            <ul className="mt-6 space-y-3">
              {ONE_ON_ONE_POINTS.map((point) => (
                <li key={point} className="flex items-start gap-3 text-sm">
                  <Check className="text-primary mt-0.5 size-4 shrink-0" aria-hidden="true" />
                  <span>{point}</span>
                </li>
              ))}
            </ul>
          </div>

          <div className="ring-border/60 relative aspect-[3/2] w-full overflow-hidden rounded-xl shadow-xl ring-1">
            <Image
              src="/images/needs/one-on-one.jpg"
              alt="Live 1-on-1 yoga class over Zoom with a certified teacher"
              fill
              sizes="(max-width: 1024px) 100vw, 50vw"
              className="object-cover"
            />
            <span className="bg-background/85 text-foreground absolute bottom-3 left-3 rounded-full px-3 py-1 text-xs font-medium backdrop-blur">
              Live 1-on-1 yoga classes via Zoom with a certified teacher
            </span>
          </div>
        </div>
      </section>

      {/* FAQ */}
      <section className="mx-auto max-w-3xl px-6 py-14">
        <h2 className="text-center text-2xl font-semibold tracking-tight sm:text-3xl">
          Frequently asked questions
        </h2>
        <Accordion className="mt-8">
          {FAQ.map((item) => (
            <AccordionItem key={item.q} value={item.q}>
              <AccordionTrigger>{item.q}</AccordionTrigger>
              <AccordionContent>{item.a}</AccordionContent>
            </AccordionItem>
          ))}
        </Accordion>
      </section>

      {/* Final CTA */}
      <section className="bg-primary text-primary-foreground">
        <div className="mx-auto max-w-4xl px-6 py-16 text-center">
          <h2 className="text-3xl font-semibold tracking-tight text-balance">
            Begin your yoga journey today
          </h2>
          <p className="mt-3 text-lg opacity-90">
            Join Dhyan Mitra and practise with guidance, structure and support.
          </p>
          <div className="mt-8">
            <Link
              href="/register"
              className={cn(buttonVariants({ size: "lg", variant: "secondary" }))}
            >
              Create your free account
            </Link>
          </div>
        </div>
      </section>
    </PublicShell>
  );
}
