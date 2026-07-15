/**
 * Best-effort YouTube/Vimeo watch-URL -> embed-URL conversion so a lesson's
 * stored videoUrl (Ref: SRS 7.8 - just an external URL string, no format
 * validation server-side) renders inline instead of requiring the viewer to
 * already have an embed-shaped link. Falls through unchanged for anything
 * else (already an embed URL, or an unrecognized host).
 */
export function toEmbedUrl(url: string): string {
  try {
    const parsed = new URL(url);

    if (parsed.hostname.includes("youtube.com") && parsed.searchParams.has("v")) {
      return `https://www.youtube.com/embed/${parsed.searchParams.get("v")}`;
    }
    if (parsed.hostname === "youtu.be") {
      return `https://www.youtube.com/embed${parsed.pathname}`;
    }
    if (parsed.hostname.includes("vimeo.com") && !parsed.pathname.includes("/embed/")) {
      return `https://player.vimeo.com/video${parsed.pathname}`;
    }

    return url;
  } catch {
    return url;
  }
}
