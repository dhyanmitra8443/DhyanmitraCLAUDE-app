/**
 * The backend returns public upload URLs (e.g. profile photos) as a
 * relative path meant for its own origin (`/uploads/...`), which resolves
 * incorrectly if rendered as-is in the browser against the frontend's
 * origin. Route those through the unauthenticated `/api/uploads` proxy;
 * leave absolute URLs (external thumbnails, etc.) untouched.
 */
export function resolveUploadUrl(url: string | null | undefined): string | undefined {
  if (!url) return undefined;
  return url.startsWith("/uploads/") ? `/api${url}` : url;
}
