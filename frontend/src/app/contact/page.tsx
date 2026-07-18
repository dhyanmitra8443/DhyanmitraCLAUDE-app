import { Mail } from "lucide-react";
import { PublicShell } from "@/components/marketing/public-shell";
import { SITE } from "@/lib/site";

export const metadata = { title: "Contact Us | Dhyan Mitra" };

export default function ContactPage() {
  return (
    <PublicShell>
      <div className="mx-auto max-w-3xl px-6 py-14">
        <h1 className="text-3xl font-semibold tracking-tight">Contact us</h1>
        <p className="text-muted-foreground mt-3">
          We&apos;d love to hear from you. Reach out with any questions about courses, live classes
          or certification.
        </p>

        <dl className="mt-10 space-y-6">
          <div>
            <dt className="text-sm font-semibold tracking-tight">Email</dt>
            <dd className="mt-1">
              <a
                href={`mailto:${SITE.email}`}
                className="text-primary inline-flex items-center gap-2 hover:underline"
              >
                <Mail className="size-4" aria-hidden="true" />
                {SITE.email}
              </a>
            </dd>
          </div>

          <div>
            <dt className="text-sm font-semibold tracking-tight">Facebook</dt>
            <dd className="mt-1">
              <a
                href={SITE.facebookUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="text-primary hover:underline"
              >
                Message us on Facebook
              </a>
            </dd>
          </div>

          <div>
            <dt className="text-sm font-semibold tracking-tight">Proprietor</dt>
            <dd className="text-muted-foreground mt-1">{SITE.proprietor}</dd>
          </div>

          <div>
            <dt className="text-sm font-semibold tracking-tight">Organisation</dt>
            <dd className="text-muted-foreground mt-1">{SITE.legalName}</dd>
          </div>
        </dl>
      </div>
    </PublicShell>
  );
}
