import { Mail, Phone, MapPin, Globe } from "lucide-react";
import { PublicShell } from "@/components/marketing/public-shell";
import { SITE, activeSocials } from "@/lib/site";
import { SOCIAL_ICONS } from "@/components/marketing/social-icons";

export const metadata = {
  title: "Contact Us | Dhyan Mitra",
  description: "Get in touch with Dhyan Mitra about courses, live classes and certification.",
};

export default function ContactPage() {
  const socials = activeSocials();

  return (
    <PublicShell>
      <section className="border-border/60 bg-card/50 border-b">
        <div className="mx-auto max-w-3xl px-6 py-14 text-center">
          <p className="text-(--brand-orange) text-sm font-semibold tracking-wide uppercase">Contact</p>
          <h1 className="text-primary mt-3 text-3xl font-bold tracking-tight sm:text-4xl">Contact us</h1>
          <p className="text-muted-foreground mt-4">
            We&apos;d love to hear from you. Reach out with any questions about courses, live classes
            or certification.
          </p>
        </div>
      </section>

      <div className="mx-auto max-w-3xl px-6 py-14">
        <dl className="grid gap-6 sm:grid-cols-2">
          <div className="bg-card ring-border/60 rounded-xl p-5 ring-1">
            <dt className="text-primary flex items-center gap-2 text-sm font-semibold">
              <Mail className="size-4" aria-hidden="true" /> Email
            </dt>
            <dd className="mt-2">
              <a href={`mailto:${SITE.email}`} className="hover:underline">
                {SITE.email}
              </a>
            </dd>
          </div>

          <div className="bg-card ring-border/60 rounded-xl p-5 ring-1">
            <dt className="text-primary flex items-center gap-2 text-sm font-semibold">
              <Phone className="size-4" aria-hidden="true" /> Phone
            </dt>
            <dd className="mt-2">
              <a href={`tel:${SITE.phone.replace(/\s+/g, "")}`} className="hover:underline">
                {SITE.phone}
              </a>
            </dd>
          </div>

          <div className="bg-card ring-border/60 rounded-xl p-5 ring-1">
            <dt className="text-primary flex items-center gap-2 text-sm font-semibold">
              <MapPin className="size-4" aria-hidden="true" /> Location
            </dt>
            <dd className="text-muted-foreground mt-2">{SITE.location}</dd>
          </div>

          <div className="bg-card ring-border/60 rounded-xl p-5 ring-1">
            <dt className="text-primary flex items-center gap-2 text-sm font-semibold">
              <Globe className="size-4" aria-hidden="true" /> Website
            </dt>
            <dd className="mt-2">
              <a href={`https://${SITE.domain}`} className="hover:underline">
                www.{SITE.domain}
              </a>
            </dd>
          </div>
        </dl>

        {socials.length > 0 && (
          <div className="mt-8">
            <p className="text-sm font-semibold">Follow us</p>
            <div className="mt-3 flex items-center gap-3">
              {socials.map(([key, url]) => {
                const Icon = SOCIAL_ICONS[key];
                return (
                  <a
                    key={key}
                    href={url}
                    target="_blank"
                    rel="noopener noreferrer"
                    aria-label={key}
                    className="bg-primary/10 text-primary hover:bg-primary/20 rounded-full p-2.5"
                  >
                    <Icon className="size-4" />
                  </a>
                );
              })}
            </div>
          </div>
        )}

        <p className="text-muted-foreground mt-10 text-sm">
          Proprietor: {SITE.proprietor} · {SITE.legalName}
        </p>
      </div>
    </PublicShell>
  );
}
