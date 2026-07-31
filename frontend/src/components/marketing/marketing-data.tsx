import type { ComponentType } from "react";
import {
  BadgeCheck,
  Flower2,
  Compass,
  Users,
  Leaf,
  GraduationCap,
  Wind,
  Brain,
  HeartPulse,
  Sparkles,
  CalendarDays,
  ShieldCheck,
  MonitorPlay,
  Award,
} from "lucide-react";

type Icon = ComponentType<{ className?: string }>;

// The five trust points shown in the strip beneath the hero. Qualitative
// claims only — no invented numbers.
export const FEATURES: { icon: Icon; title: string; sub: string }[] = [
  { icon: BadgeCheck, title: "Certified Trainers", sub: "Experienced & verified" },
  { icon: Flower2, title: "Holistic Approach", sub: "Ancient wisdom + modern science" },
  { icon: Compass, title: "Personalized Guidance", sub: "For every individual" },
  { icon: Users, title: "Community Support", sub: "Grow together" },
  { icon: Leaf, title: "100% Natural", sub: "Pure · safe · effective" },
];

// "Programs for Every Need." tone selects the icon-badge colour.
export const PROGRAMS: {
  slug: string;
  icon: Icon;
  title: string;
  desc: string;
  tone: "green" | "orange" | "blue";
}[] = [
  {
    slug: "yoga-classes",
    icon: GraduationCap,
    title: "Yoga Classes",
    desc: "Hatha, Ashtanga, Vinyasa & more, for all levels.",
    tone: "green",
  },
  {
    slug: "pranayama",
    icon: Wind,
    title: "Pranayama",
    desc: "Breath techniques for energy, immunity & lung health.",
    tone: "orange",
  },
  {
    slug: "meditation",
    icon: Brain,
    title: "Meditation",
    desc: "Reduce stress, improve focus & experience inner calm.",
    tone: "blue",
  },
  {
    slug: "wellness-coaching",
    icon: HeartPulse,
    title: "Wellness Coaching",
    desc: "Natural lifestyle, diet, detox & overall well-being.",
    tone: "green",
  },
  {
    slug: "spiritual-growth",
    icon: Sparkles,
    title: "Spiritual Growth",
    desc: "Explore the deeper meaning of life and inner awakening.",
    tone: "orange",
  },
  {
    slug: "workshops-retreats",
    icon: CalendarDays,
    title: "Workshops & Retreats",
    desc: "Join our special events and life-transforming retreats.",
    tone: "blue",
  },
];

// Honest replacements for the mockup's fabricated stat counters — qualitative
// highlights, no numbers presented as facts.
export const HIGHLIGHTS: { icon: Icon; title: string; sub: string }[] = [
  { icon: BadgeCheck, title: "Certified Trainers", sub: "Experienced & verified teachers" },
  { icon: MonitorPlay, title: "Live & Recorded", sub: "Learn live or at your own pace" },
  { icon: Award, title: "Verifiable Certificates", sub: "Recognised on completion" },
  { icon: ShieldCheck, title: "Practices for Every Level", sub: "Beginner to advanced" },
];

// "Upcoming Events" list. Honest cadence badges (Daily/Weekly/Monthly) rather
// than invented calendar dates — these describe the kinds of sessions run.
export const EVENTS: {
  badge: string;
  tone: "green" | "orange" | "blue";
  title: string;
  desc: string;
  href: string;
  cta: string;
}[] = [
  {
    badge: "Daily",
    tone: "green",
    title: "Live Online Yoga Classes",
    desc: "Instructor-led sessions you join over video, for every level.",
    href: "/register",
    cta: "Join now",
  },
  {
    badge: "Weekly",
    tone: "orange",
    title: "Guided Meditation Sessions",
    desc: "Group practice to quiet the mind and reduce stress.",
    href: "/register",
    cta: "Join now",
  },
  {
    badge: "Monthly",
    tone: "blue",
    title: "Wellness & Detox Workshops",
    desc: "Focused workshops on lifestyle, diet and well-being.",
    href: "/events",
    cta: "Learn more",
  },
];

// Icon-badge colour classes keyed by tone.
export const TONE_BADGE: Record<"green" | "orange" | "blue", string> = {
  green: "bg-[#2f9e5e] text-white",
  orange: "bg-[#e8871e] text-white",
  blue: "bg-[#3a92c4] text-white",
};

export const TONE_LINK: Record<"green" | "orange" | "blue", string> = {
  green: "text-[#1e6b3f]",
  orange: "text-[#b45309]",
  blue: "text-[#2b6f97]",
};
