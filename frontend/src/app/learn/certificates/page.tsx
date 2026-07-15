import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { CertificateDownloadButton } from "@/components/certificates/certificate-download-button";
import { getMyCertificates } from "@/lib/certificates/queries";

export const metadata = { title: "Certificates | Dhyan Mitra" };

/** Ref: SRS 12.11 - the authenticated student's own earned certificates. */
export default async function StudentCertificatesPage() {
  const certificates = await getMyCertificates();

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Certificates</h1>

      <Card>
        <CardHeader>
          <CardTitle>Your certificates</CardTitle>
        </CardHeader>
        <CardContent>
          {certificates.length === 0 ? (
            <p className="text-muted-foreground text-sm">
              Complete a course to earn your first certificate.{" "}
              <Link href="/learn" className="underline underline-offset-2">
                Go to dashboard
              </Link>
              .
            </p>
          ) : (
            <ul className="space-y-2">
              {certificates.map((certificate) => (
                <li key={certificate.id} className="flex items-center justify-between gap-4 rounded-lg border px-3 py-2">
                  <div>
                    <p className="text-sm font-medium">{certificate.courseName}</p>
                    <p className="text-muted-foreground text-xs">
                      {certificate.certificateNumber} · Issued {certificate.issueDate}
                    </p>
                  </div>
                  {certificate.id && <CertificateDownloadButton certificateId={certificate.id} />}
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
