import Link from "next/link";
import { Mail } from "lucide-react";
import { SITE } from "@/lib/site";

// lucide-react v1 dropped brand glyphs, so the Facebook mark is inlined.
function FacebookIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" fill="currentColor" className={className}>
      <path d="M24 12.07C24 5.4 18.63 0 12 0S0 5.4 0 12.07C0 18.1 4.39 23.1 10.13 24v-8.44H7.08v-3.49h3.05V9.41c0-3.02 1.79-4.69 4.53-4.69 1.31 0 2.68.24 2.68.24v2.97h-1.51c-1.49 0-1.95.93-1.95 1.89v2.25h3.32l-.53 3.49h-2.79V24C19.61 23.1 24 18.1 24 12.07Z" />
    </svg>
  );
}

export function SiteFooter() {
  return (
    <footer className="border-border/60 bg-card/40 mt-auto border-t">
      <div className="mx-auto grid max-w-6xl gap-8 px-6 py-10 sm:grid-cols-3">
        <div>
          <h3 className="text-sm font-semibold tracking-tight">Contact us</h3>
          <ul className="text-muted-foreground mt-3 space-y-2 text-sm">
            <li>
              <a
                href={`mailto:${SITE.email}`}
                className="hover:text-foreground inline-flex items-center gap-2"
              >
                <Mail className="size-4 shrink-0" aria-hidden="true" />
                {SITE.email}
              </a>
            </li>
            <li>
              <a
                href={SITE.facebookUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="hover:text-foreground inline-flex items-center gap-2"
              >
                <FacebookIcon className="size-4 shrink-0" />
                Facebook
              </a>
            </li>
            <li className="text-xs">Proprietor: {SITE.proprietor}</li>
          </ul>
        </div>

        <div>
          <h3 className="text-sm font-semibold tracking-tight">Legal</h3>
          <ul className="text-muted-foreground mt-3 space-y-2 text-sm">
            <li>
              <Link href="/terms" className="hover:text-foreground">
                Terms &amp; Conditions
              </Link>
            </li>
            <li>
              <Link href="/privacy" className="hover:text-foreground">
                Privacy &amp; Security
              </Link>
            </li>
            <li>
              <Link href="/contact" className="hover:text-foreground">
                Contact Us
              </Link>
            </li>
          </ul>
        </div>

        <div>
          <h3 className="text-sm font-semibold tracking-tight">Explore</h3>
          <ul className="text-muted-foreground mt-3 space-y-2 text-sm">
            <li>
              <Link href="/courses" className="hover:text-foreground">
                Courses
              </Link>
            </li>
            <li>
              <Link href="/sign-in" className="hover:text-foreground">
                Sign in
              </Link>
            </li>
            <li>
              <Link href="/register" className="hover:text-foreground">
                Create an account
              </Link>
            </li>
          </ul>
        </div>
      </div>

      <div className="border-border/60 border-t">
        <p className="text-muted-foreground mx-auto max-w-6xl px-6 py-4 text-center text-xs">
          © {new Date().getFullYear()} {SITE.legalName}. All rights reserved.
        </p>
      </div>
    </footer>
  );
}
