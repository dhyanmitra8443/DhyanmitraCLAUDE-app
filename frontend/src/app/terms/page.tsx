import { PublicShell } from "@/components/marketing/public-shell";
import { SITE } from "@/lib/site";

export const metadata = { title: "Terms & Conditions | Dhyan Mitra" };

// NOTE: Placeholder boilerplate pending review by a qualified professional.
// Replace this copy with the business's finalised legal terms before launch.
export default function TermsPage() {
  return (
    <PublicShell>
      <div className="mx-auto max-w-3xl px-6 py-14">
        <h1 className="text-3xl font-semibold tracking-tight">Terms &amp; Conditions</h1>
        <p className="text-muted-foreground mt-2 text-sm">Last updated: {new Date().getFullYear()}</p>

        <div className="border-primary/30 bg-primary/10 text-muted-foreground mt-6 rounded-lg border px-4 py-3 text-sm">
          These terms are provided as a starting template and should be reviewed by a qualified
          professional before you rely on them.
        </div>

        <div className="mt-8 space-y-8 text-sm leading-relaxed">
          <section>
            <h2 className="text-foreground text-lg font-semibold">1. Acceptance of terms</h2>
            <p className="text-muted-foreground mt-2">
              By accessing or using {SITE.name} ({SITE.legalName}), you agree to be bound by these
              Terms &amp; Conditions. If you do not agree, please do not use the platform.
            </p>
          </section>

          <section>
            <h2 className="text-foreground text-lg font-semibold">2. Accounts</h2>
            <p className="text-muted-foreground mt-2">
              You are responsible for maintaining the confidentiality of your account credentials and
              for all activity that occurs under your account. You must provide accurate information
              when registering and keep it up to date.
            </p>
          </section>

          <section>
            <h2 className="text-foreground text-lg font-semibold">3. Courses and content</h2>
            <p className="text-muted-foreground mt-2">
              Course materials, live classes and other content are provided for your personal,
              non-commercial use. You may not copy, redistribute or resell any content without prior
              written permission.
            </p>
          </section>

          <section>
            <h2 className="text-foreground text-lg font-semibold">4. Payments and refunds</h2>
            <p className="text-muted-foreground mt-2">
              Fees for paid courses and subscriptions are shown at the point of purchase. Any refund
              eligibility and process will be described at checkout or communicated to you directly.
            </p>
          </section>

          <section>
            <h2 className="text-foreground text-lg font-semibold">5. Health disclaimer</h2>
            <p className="text-muted-foreground mt-2">
              Yoga and related practices carry inherent risks. Consult a physician before beginning
              any exercise programme. {SITE.name} is not liable for any injury arising from your
              practice; participate within your own limits.
            </p>
          </section>

          <section>
            <h2 className="text-foreground text-lg font-semibold">6. Changes to these terms</h2>
            <p className="text-muted-foreground mt-2">
              We may update these terms from time to time. Continued use of the platform after
              changes take effect constitutes acceptance of the revised terms.
            </p>
          </section>

          <section>
            <h2 className="text-foreground text-lg font-semibold">7. Contact</h2>
            <p className="text-muted-foreground mt-2">
              Questions about these terms can be sent to{" "}
              <a href={`mailto:${SITE.email}`} className="text-primary hover:underline">
                {SITE.email}
              </a>
              .
            </p>
          </section>
        </div>
      </div>
    </PublicShell>
  );
}
