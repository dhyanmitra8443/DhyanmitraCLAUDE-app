import type { ReactNode } from "react";
import { SiteHeader } from "./site-header";
import { SiteFooter } from "./site-footer";

// Wraps every public/marketing page: applies the brand theme (`theme-brand`,
// scoped here so dashboards stay neutral) and frames content with the shared
// header and footer.
export function PublicShell({ children }: { children: ReactNode }) {
  return (
    <div className="theme-brand bg-background text-foreground flex min-h-screen flex-col">
      <SiteHeader />
      <div className="flex-1">{children}</div>
      <SiteFooter />
    </div>
  );
}
