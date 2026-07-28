import Link from "next/link";
import { Mail } from "lucide-react";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { PublicShell } from "@/components/marketing/public-shell";
import { SITE } from "@/lib/site";

export const metadata = {
  title: "Blog | Dhyan Mitra",
  description: "Yoga, meditation and wellness insights from Dhyan Mitra — coming soon.",
};

export default function BlogPage() {
  return (
    <PublicShell>
      <section className="mx-auto max-w-2xl px-6 py-20 text-center">
        <p className="text-(--brand-orange) text-sm font-semibold tracking-wide uppercase">Blog</p>
        <h1 className="text-primary mt-3 text-3xl font-bold tracking-tight sm:text-4xl">
          Insights &amp; articles are on the way
        </h1>
        <p className="text-muted-foreground mt-4">
          We&apos;re preparing articles on yoga, pranayama, meditation and holistic wellness. In the
          meantime, follow us or reach out — we&apos;d love to hear what you&apos;d like us to write
          about.
        </p>
        <div className="mt-8 flex flex-wrap justify-center gap-3">
          <a
            href={`mailto:${SITE.email}?subject=${encodeURIComponent("Blog topic suggestion")}`}
            className={cn(buttonVariants({ size: "lg" }), "gap-2")}
          >
            <Mail className="size-5" aria-hidden="true" />
            Suggest a topic
          </a>
          <Link href="/courses" className={cn(buttonVariants({ size: "lg", variant: "outline" }))}>
            Browse courses
          </Link>
        </div>
      </section>
    </PublicShell>
  );
}
