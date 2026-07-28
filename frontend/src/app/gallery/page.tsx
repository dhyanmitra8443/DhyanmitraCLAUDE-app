import Link from "next/link";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { PublicShell } from "@/components/marketing/public-shell";
import { AutoplayVideo } from "@/components/marketing/autoplay-video";

export const metadata = {
  title: "Gallery | Dhyan Mitra",
  description: "Watch practice sessions and techniques from Dhyan Mitra's yoga and meditation classes.",
};

const VIDEOS = [
  { src: "/videos/video-1.mp4", label: "Practice session 1" },
  { src: "/videos/video-2.mp4", label: "Practice session 2" },
  { src: "/videos/video-3.mp4", label: "Practice session 3" },
  { src: "/videos/video-4.mp4", label: "Practice session 4" },
  { src: "/videos/video-5.mp4", label: "Practice session 5" },
  { src: "/videos/video-6.mp4", label: "Practice session 6" },
  { src: "/videos/video-7.mp4", label: "Practice session 7" },
];

export default function GalleryPage() {
  return (
    <PublicShell>
      <section className="border-border/60 bg-card/50 border-b">
        <div className="mx-auto max-w-3xl px-6 py-14 text-center">
          <p className="text-(--brand-orange) text-sm font-semibold tracking-wide uppercase">Gallery</p>
          <h1 className="text-primary mt-3 text-3xl font-bold tracking-tight sm:text-4xl">
            See the practice in action
          </h1>
          <p className="text-muted-foreground mt-4">
            A glimpse of the sessions and techniques you&apos;ll learn with Dhyan Mitra. Tap the
            speaker icon on any clip to hear it.
          </p>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-6 py-16">
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {VIDEOS.map((video) => (
            <AutoplayVideo key={video.src} src={video.src} label={video.label} />
          ))}
        </div>

        <div className="mt-12 text-center">
          <Link href="/register" className={cn(buttonVariants({ size: "lg" }))}>
            Start Your Journey
          </Link>
        </div>
      </section>
    </PublicShell>
  );
}
