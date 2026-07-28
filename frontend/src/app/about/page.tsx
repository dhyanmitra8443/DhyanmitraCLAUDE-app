import Link from "next/link";
import { Check } from "lucide-react";
import { buttonVariants } from "@/components/ui/button";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import { cn } from "@/lib/utils";
import { PublicShell } from "@/components/marketing/public-shell";
import { AutoplayVideo } from "@/components/marketing/autoplay-video";
import { FEATURES } from "@/components/marketing/marketing-data";
import { SITE } from "@/lib/site";

export const metadata = {
  title: "About Us | Dhyan Mitra",
  description:
    "Learn about Dhyan Mitra — our mission to bring authentic yoga, meditation and wellness practices to everyone.",
};

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

export default function AboutPage() {
  return (
    <PublicShell>
      {/* Intro */}
      <section className="mx-auto grid max-w-6xl items-center gap-10 px-6 py-16 lg:grid-cols-2 lg:py-20">
        <div>
          <p className="text-(--brand-orange) text-sm font-semibold tracking-wide uppercase">
            About {SITE.name}
          </p>
          <h1 className="text-primary mt-3 text-3xl font-bold tracking-tight sm:text-4xl">
            Empowering lives through yoga &amp; ancient wisdom
          </h1>
          <p className="text-muted-foreground mt-5">
            {SITE.legalName} was founded on a simple belief: authentic yoga and meditation should be
            within everyone&apos;s reach. We combine time-tested practice with modern, structured
            learning — recorded courses, live instructor-led classes, and personal guidance — so you
            can build a balanced, peaceful and healthy life at your own pace.
          </p>
          <p className="text-muted-foreground mt-4">
            Under the guidance of {SITE.proprietor}, our certified teachers meet every student where
            they are, whatever brought them to the mat.
          </p>
        </div>
        <AutoplayVideo src="/videos/video-3.mp4" label="Yoga practice at Dhyan Mitra" className="shadow-lg" />
      </section>

      {/* Values */}
      <section className="bg-card/50 border-border/60 border-y">
        <div className="mx-auto max-w-6xl px-6 py-16">
          <h2 className="text-primary text-center text-2xl font-bold tracking-tight sm:text-3xl">
            What we stand for
          </h2>
          <div className="mt-10 grid gap-6 sm:grid-cols-2 lg:grid-cols-5">
            {FEATURES.map((feature) => (
              <div key={feature.title} className="bg-card ring-border/60 rounded-xl p-5 text-center ring-1">
                <feature.icon className="text-primary mx-auto size-7" aria-hidden="true" />
                <p className="mt-3 font-semibold">{feature.title}</p>
                <p className="text-muted-foreground mt-1 text-xs">{feature.sub}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Why us */}
      <section className="mx-auto max-w-3xl px-6 py-16">
        <h2 className="text-primary text-2xl font-bold tracking-tight sm:text-3xl">Why practise with us</h2>
        <ul className="mt-6 space-y-3">
          {[
            "Structured courses labelled Beginner, Intermediate and Advanced",
            "Live instructor-led classes you can join and ask questions in",
            "Verifiable certificate of completion for eligible courses",
            "Learn on any device, at your own pace, wherever you are",
          ].map((point) => (
            <li key={point} className="flex items-start gap-3 text-sm">
              <Check className="text-primary mt-0.5 size-4 shrink-0" aria-hidden="true" />
              <span>{point}</span>
            </li>
          ))}
        </ul>
      </section>

      {/* FAQ */}
      <section className="bg-card/50 border-border/60 border-y">
        <div className="mx-auto max-w-3xl px-6 py-16">
          <h2 className="text-primary text-center text-2xl font-bold tracking-tight sm:text-3xl">
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
        </div>
      </section>

      {/* CTA */}
      <section className="mx-auto max-w-4xl px-6 py-16 text-center">
        <h2 className="text-primary text-2xl font-bold tracking-tight sm:text-3xl">
          Ready to begin your journey?
        </h2>
        <div className="mt-6 flex flex-wrap justify-center gap-3">
          <Link href="/register" className={cn(buttonVariants({ size: "lg" }))}>
            Start Your Journey
          </Link>
          <Link href="/courses" className={cn(buttonVariants({ size: "lg", variant: "outline" }))}>
            Browse courses
          </Link>
        </div>
      </section>
    </PublicShell>
  );
}
