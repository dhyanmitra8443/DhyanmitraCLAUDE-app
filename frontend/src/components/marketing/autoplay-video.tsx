"use client";

import { useEffect, useRef, useState } from "react";
import { Volume2, VolumeX } from "lucide-react";

// Autoplaying, looping, muted-by-default video with a speaker toggle.
//
// Playback is JS-driven (no `autoPlay` attribute) and gated on an
// IntersectionObserver, so a clip only plays — and only buffers — while it's
// actually on screen and the tab is visible. That satisfies "stop when the
// user leaves" (route change unmounts the <video>; a hidden tab or scrolling
// away pauses it; returning resumes) and keeps bandwidth sane even though one
// source clip is ~38MB — off-screen clips never download.
export function AutoplayVideo({
  src,
  label,
  className,
}: {
  src: string;
  label: string;
  className?: string;
}) {
  const videoRef = useRef<HTMLVideoElement>(null);
  const inViewRef = useRef(false);
  const [muted, setMuted] = useState(true);

  useEffect(() => {
    const video = videoRef.current;
    if (!video) return;

    // React can be unreliable about reflecting the `muted` prop onto the DOM
    // node, so assert it here — muted playback is what browsers allow to
    // autoplay without a user gesture.
    video.muted = true;

    const tryPlay = () => {
      if (!inViewRef.current || document.hidden) return;
      video.play().catch(() => {
        // A browser may still refuse (e.g. the user unmuted earlier). Fall
        // back to muted so the video always plays across browsers.
        video.muted = true;
        setMuted(true);
        video.play().catch(() => {});
      });
    };

    const observer = new IntersectionObserver(
      ([entry]) => {
        inViewRef.current = entry.isIntersecting;
        if (entry.isIntersecting) tryPlay();
        else video.pause();
      },
      { threshold: 0.4 },
    );
    observer.observe(video);

    const onVisibilityChange = () => {
      if (document.hidden) video.pause();
      else tryPlay();
    };
    document.addEventListener("visibilitychange", onVisibilityChange);

    return () => {
      observer.disconnect();
      document.removeEventListener("visibilitychange", onVisibilityChange);
      video.pause();
    };
  }, []);

  function toggleMute() {
    const video = videoRef.current;
    if (!video) return;
    const next = !video.muted;
    video.muted = next;
    setMuted(next);
    // Unmuting counts as the user gesture that lets audio play, so (re)start.
    if (!next) void video.play().catch(() => {});
  }

  return (
    <div
      className={
        "bg-secondary ring-border/60 relative aspect-video w-full overflow-hidden rounded-xl ring-1 " +
        (className ?? "")
      }
    >
      <video
        ref={videoRef}
        src={src}
        muted
        loop
        playsInline
        preload="metadata"
        aria-label={label}
        className="h-full w-full bg-black object-cover"
      />
      <button
        type="button"
        onClick={toggleMute}
        aria-label={muted ? `Unmute ${label}` : `Mute ${label}`}
        aria-pressed={!muted}
        className="bg-background/80 text-foreground ring-border/60 hover:bg-background absolute right-3 bottom-3 flex size-9 items-center justify-center rounded-full ring-1 backdrop-blur transition-colors"
      >
        {muted ? <VolumeX className="size-4" aria-hidden="true" /> : <Volume2 className="size-4" aria-hidden="true" />}
      </button>
    </div>
  );
}
