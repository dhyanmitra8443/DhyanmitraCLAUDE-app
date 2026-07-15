"use client";

import { useState } from "react";

export function CourseThumbnail({ src, className }: { src: string | null | undefined; className?: string }) {
  const [failed, setFailed] = useState(false);

  if (!src || failed) return null;

  return (
    // eslint-disable-next-line @next/next/no-img-element -- arbitrary externally-hosted URL, not a configured next/image domain
    <img src={src} alt="" className={className} onError={() => setFailed(true)} />
  );
}
