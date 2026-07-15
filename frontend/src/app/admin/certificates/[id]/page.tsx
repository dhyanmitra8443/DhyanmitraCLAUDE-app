import Link from "next/link";
import { notFound } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { CertificateDownloadButton } from "@/components/certificates/certificate-download-button";
import { getCertificateDetail } from "@/lib/certificates/queries";
import { ApiError } from "@/lib/api/errors";

export const metadata = { title: "Certificate | Dhyan Mitra" };

export default async function AdminCertificateDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  let certificate;
  try {
    certificate = await getCertificateDetail(id);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) notFound();
    throw error;
  }

  return (
    <div className="max-w-2xl space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">{certificate.certificateNumber}</h1>
          <p className="text-muted-foreground text-sm">{certificate.studentName}</p>
        </div>
        {certificate.id && <CertificateDownloadButton certificateId={certificate.id} />}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Details</CardTitle>
        </CardHeader>
        <CardContent className="space-y-1 text-sm">
          <p>
            <span className="text-muted-foreground">Course: </span>
            {certificate.courseId ? (
              <Link href={`/admin/courses/${certificate.courseId}`} className="underline underline-offset-2">
                {certificate.courseName}
              </Link>
            ) : (
              certificate.courseName
            )}
          </p>
          <p>
            <span className="text-muted-foreground">Instructors: </span>
            {(certificate.instructorNames ?? []).join(", ") || "—"}
          </p>
          <p>
            <span className="text-muted-foreground">Completion date: </span>
            {certificate.completionDate}
          </p>
          <p>
            <span className="text-muted-foreground">Issue date: </span>
            {certificate.issueDate}
          </p>
          {certificate.verificationId && (
            <p>
              <span className="text-muted-foreground">Verification: </span>
              <Link href={`/verify/${certificate.verificationId}`} className="underline underline-offset-2">
                /verify/{certificate.verificationId}
              </Link>
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
