import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { verifyCertificate } from "@/lib/certificates/queries";
import { ApiError } from "@/lib/api/errors";

export const metadata = { title: "Verify certificate | Dhyan Mitra" };

/** Ref: SRS 12.13, 12.17 - public, unauthenticated. Unknown IDs reveal nothing beyond "not found". */
export default async function VerifyCertificatePage({ params }: { params: Promise<{ verificationId: string }> }) {
  const { verificationId } = await params;

  let result;
  try {
    result = await verifyCertificate(verificationId);
  } catch (error) {
    if (!(error instanceof ApiError && error.status === 404)) throw error;
  }

  return (
    <div className="mx-auto max-w-lg space-y-6 px-6 py-16">
      <div className="text-center">
        <Link href="/" className="text-muted-foreground text-sm hover:underline">
          Dhyan Mitra
        </Link>
        <h1 className="mt-2 text-2xl font-semibold tracking-tight">Certificate verification</h1>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center justify-between">
            Result
            <Badge variant={result ? "default" : "destructive"}>{result ? "Valid" : "Not found"}</Badge>
          </CardTitle>
        </CardHeader>
        <CardContent>
          {result ? (
            <dl className="space-y-2 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Student</dt>
                <dd className="font-medium">{result.studentName}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Course</dt>
                <dd className="font-medium">{result.courseName}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Completion date</dt>
                <dd className="font-medium">{result.completionDate}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Issue date</dt>
                <dd className="font-medium">{result.issueDate}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Certificate #</dt>
                <dd className="font-medium">{result.certificateNumber}</dd>
              </div>
            </dl>
          ) : (
            <p className="text-muted-foreground text-sm">
              This verification link doesn&apos;t match any issued certificate.
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
