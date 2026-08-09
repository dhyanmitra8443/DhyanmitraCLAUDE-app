import type { ReactNode } from "react";
import { SiteHeader } from "./site-header";
import { SiteFooter } from "./site-footer";
import { getSession } from "@/lib/auth/session";

/**
 * Where "Courses" points once signed in. Students browse the same public
 * catalog they always could (`/courses`); staff go to their own management
 * list, matching the hrefs already used in the learn/teach/admin layouts.
 */
const COURSES_HREF_FOR_ROLE = {
  STUDENT: "/courses",
  INSTRUCTOR: "/teach/courses",
  ADMINISTRATOR: "/admin/courses",
} as const;

// Wraps every public/marketing page: applies the brand theme (`theme-brand`,
// shared with the dashboards via AppShell) and frames content with the shared
// header and footer.
//
// Reading the session here makes these pages dynamically rendered rather than
// static, which is the cost of deciding the nav server-side. It has to be
// server-side: the session cookie is httpOnly, so the client cannot read it,
// and a client-side check would flash the wrong menu on first paint.
export async function PublicShell({ children }: { children: ReactNode }) {
  const session = await getSession();
  const coursesHref = session ? COURSES_HREF_FOR_ROLE[session.role] : null;

  return (
    <div className="theme-brand bg-background text-foreground flex min-h-screen flex-col">
      <SiteHeader coursesHref={coursesHref} />
      <div className="flex-1">{children}</div>
      <SiteFooter coursesHref={coursesHref} />
    </div>
  );
}
