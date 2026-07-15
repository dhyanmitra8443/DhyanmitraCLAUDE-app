import Link from "next/link";
import { Card, CardContent } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { CertificatesFilterBar } from "@/components/certificates/certificates-filter-bar";
import { PaginationControls } from "@/components/shared/pagination-controls";
import { searchCertificates } from "@/lib/certificates/queries";

export const metadata = { title: "Certificates | Dhyan Mitra" };

export default async function AdminCertificatesPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const page = Number(params.page ?? 0);
  const studentName = typeof params.studentName === "string" ? params.studentName : undefined;
  const courseId = typeof params.courseId === "string" ? params.courseId : undefined;
  const certificateNumber = typeof params.certificateNumber === "string" ? params.certificateNumber : undefined;

  const result = await searchCertificates({
    page,
    size: 20,
    sort: "issueDate,desc",
    studentName,
    courseId,
    certificateNumber,
  });

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Certificates</h1>

      <CertificatesFilterBar />

      <Card>
        <CardContent>
          {result.content.length === 0 ? (
            <p className="text-muted-foreground text-sm">No certificates match these filters.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Certificate #</TableHead>
                  <TableHead>Student</TableHead>
                  <TableHead>Course</TableHead>
                  <TableHead>Issue date</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {result.content.map((certificate) => (
                  <TableRow key={certificate.id}>
                    <TableCell className="font-medium">
                      <Link href={`/admin/certificates/${certificate.id}`} className="hover:underline">
                        {certificate.certificateNumber}
                      </Link>
                    </TableCell>
                    <TableCell>{certificate.studentName}</TableCell>
                    <TableCell>{certificate.courseName}</TableCell>
                    <TableCell>{certificate.issueDate}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <PaginationControls page={result.page.page ?? 0} totalPages={result.page.totalPages ?? 1} />
    </div>
  );
}
