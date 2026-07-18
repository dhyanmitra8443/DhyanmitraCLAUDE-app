import { PublicShell } from "@/components/marketing/public-shell";
import { SITE } from "@/lib/site";

export const metadata = { title: "Privacy & Security | Dhyan Mitra" };

// NOTE: Placeholder boilerplate pending review by a qualified professional.
// Replace this copy with the business's finalised privacy policy before launch.
export default function PrivacyPage() {
  return (
    <PublicShell>
      <div className="mx-auto max-w-3xl px-6 py-14">
        <h1 className="text-3xl font-semibold tracking-tight">Privacy &amp; Security</h1>
        <p className="text-muted-foreground mt-2 text-sm">Last updated: {new Date().getFullYear()}</p>

        <div className="border-primary/30 bg-primary/10 text-muted-foreground mt-6 rounded-lg border px-4 py-3 text-sm">
          This policy is provided as a starting template and should be reviewed by a qualified
          professional before you rely on it.
        </div>

        <div className="mt-8 space-y-8 text-sm leading-relaxed">
          <section>
            <h2 className="text-foreground text-lg font-semibold">1. Information we collect</h2>
            <p className="text-muted-foreground mt-2">
              We collect information you provide when you create an account (such as your name and
              email address), enrol in courses, and use {SITE.name}. We also collect usage data such
              as course progress to operate and improve the platform.
            </p>
          </section>

          <section>
            <h2 className="text-foreground text-lg font-semibold">2. How we use your information</h2>
            <p className="text-muted-foreground mt-2">
              Your information is used to provide and personalise your learning experience, process
              enrolments and payments, issue certificates, and communicate with you about your
              account and the service.
            </p>
          </section>

          <section>
            <h2 className="text-foreground text-lg font-semibold">3. Sharing of information</h2>
            <p className="text-muted-foreground mt-2">
              We do not sell your personal information. We may share it with service providers who
              help us operate the platform (for example, payment and email providers), only as needed
              to deliver the service.
            </p>
          </section>

          <section>
            <h2 className="text-foreground text-lg font-semibold">4. Data security</h2>
            <p className="text-muted-foreground mt-2">
              We take reasonable technical and organisational measures to protect your information.
              However, no method of transmission or storage is completely secure, and we cannot
              guarantee absolute security.
            </p>
          </section>

          <section>
            <h2 className="text-foreground text-lg font-semibold">5. Your rights</h2>
            <p className="text-muted-foreground mt-2">
              You may request access to, correction of, or deletion of your personal information by
              contacting us. We will respond in accordance with applicable law.
            </p>
          </section>

          <section>
            <h2 className="text-foreground text-lg font-semibold">6. Contact</h2>
            <p className="text-muted-foreground mt-2">
              For privacy questions or requests, contact{" "}
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
