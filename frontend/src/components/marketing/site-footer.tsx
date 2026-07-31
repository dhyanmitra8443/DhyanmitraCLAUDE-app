import Link from "next/link";
import Image from "next/image";
import { MapPin, Phone, Mail, Globe } from "lucide-react";
import { SITE, activeSocials } from "@/lib/site";
import { SOCIAL_ICONS } from "./social-icons";
import { NewsletterForm } from "./newsletter-form";

const QUICK_LINKS = [
  { href: "/", label: "Home" },
  { href: "/about", label: "About Us" },
  { href: "/courses", label: "Courses" },
  { href: "/programs", label: "Programs" },
  { href: "/events", label: "Events" },
];

const RESOURCES = [
  { href: "/blog", label: "Blog" },
  { href: "/gallery", label: "Gallery" },
  { href: "/contact", label: "Contact" },
  { href: "/privacy", label: "Privacy & Security" },
  { href: "/terms", label: "Terms & Conditions" },
];

export function SiteFooter() {
  const socials = activeSocials();

  return (
    <footer className="bg-(--brand-green-dark) mt-auto text-white/80">
      <div className="mx-auto grid max-w-6xl gap-10 px-6 py-12 sm:grid-cols-2 lg:grid-cols-4">
        {/* Brand */}
        <div>
          <div className="flex items-center gap-2.5">
            <Image
              src="/brand/dhyan-mitra-logo.png"
              alt="DYJK Dhyan Mitra logo"
              width={44}
              height={44}
              className="h-11 w-11 rounded-full object-cover"
            />
            <span className="leading-tight">
              <span className="block text-lg font-bold text-white">
                <span className="text-(--brand-orange)">{SITE.brandPrefix}</span> {SITE.name}
              </span>
              <span className="block text-[10px] tracking-[0.14em] text-white/60 uppercase">
                {SITE.institute}
              </span>
            </span>
          </div>
          <p className="mt-4 text-sm leading-relaxed">
            Empowering lives through Yoga, meditation and ancient wisdom for a healthier, happier
            world.
          </p>
          {socials.length > 0 && (
            <div className="mt-4 flex items-center gap-3">
              {socials.map(([key, url]) => {
                const Icon = SOCIAL_ICONS[key];
                return (
                  <a
                    key={key}
                    href={url}
                    target="_blank"
                    rel="noopener noreferrer"
                    aria-label={key}
                    className="rounded-full bg-white/10 p-2 hover:bg-white/20"
                  >
                    <Icon className="size-4 text-white" />
                  </a>
                );
              })}
            </div>
          )}
        </div>

        {/* Quick links */}
        <div>
          <h3 className="text-sm font-semibold text-white">Quick Links</h3>
          <ul className="mt-4 space-y-2.5 text-sm">
            {QUICK_LINKS.map((item) => (
              <li key={item.href}>
                <Link href={item.href} className="hover:text-white">
                  {item.label}
                </Link>
              </li>
            ))}
          </ul>
        </div>

        {/* Resources */}
        <div>
          <h3 className="text-sm font-semibold text-white">Resources</h3>
          <ul className="mt-4 space-y-2.5 text-sm">
            {RESOURCES.map((item) => (
              <li key={item.href}>
                <Link href={item.href} className="hover:text-white">
                  {item.label}
                </Link>
              </li>
            ))}
          </ul>
        </div>

        {/* Contact + newsletter */}
        <div>
          <h3 className="text-sm font-semibold text-white">Contact Us</h3>
          <ul className="mt-4 space-y-2.5 text-sm">
            <li className="flex items-center gap-2">
              <MapPin className="size-4 shrink-0" aria-hidden="true" />
              {SITE.location}
            </li>
            <li>
              <a href={`tel:${SITE.phone.replace(/\s+/g, "")}`} className="flex items-center gap-2 hover:text-white">
                <Phone className="size-4 shrink-0" aria-hidden="true" />
                {SITE.phone}
              </a>
            </li>
            <li>
              <a href={`mailto:${SITE.email}`} className="flex items-center gap-2 hover:text-white">
                <Mail className="size-4 shrink-0" aria-hidden="true" />
                {SITE.email}
              </a>
            </li>
            <li>
              <a
                href={`https://${SITE.domain}`}
                className="flex items-center gap-2 hover:text-white"
              >
                <Globe className="size-4 shrink-0" aria-hidden="true" />
                www.{SITE.domain}
              </a>
            </li>
          </ul>
          <h3 className="mt-6 text-sm font-semibold text-white">Newsletter</h3>
          <p className="mt-1 text-xs">Get updates on our latest programs and events.</p>
          <NewsletterForm />
        </div>
      </div>

      <div className="border-t border-white/15">
        <p className="mx-auto max-w-6xl px-6 py-4 text-center text-xs">
          © {new Date().getFullYear()} {SITE.legalName}. All rights reserved.
        </p>
      </div>
    </footer>
  );
}
