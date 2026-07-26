import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { ReferralsFilterBar } from "@/components/admin/referrals-filter-bar";
import { PaginationControls } from "@/components/shared/pagination-controls";
import { getAllReferrals } from "@/lib/referrals/queries";
import type { ReferralStatus } from "@/lib/api/types";

export const metadata = { title: "Referrals | Dhyan Mitra" };

const STATUS_VARIANT: Record<ReferralStatus, "default" | "secondary" | "outline"> = {
  PENDING: "secondary",
  ACCEPTED: "default",
  EXPIRED: "outline",
};

const STATUS_LABEL: Record<ReferralStatus, string> = {
  PENDING: "Invited",
  ACCEPTED: "Joined",
  EXPIRED: "Expired",
};

function formatDate(value: string | null): string {
  if (!value) return "—";
  return new Date(value).toLocaleDateString("en-IN", { year: "numeric", month: "short", day: "numeric" });
}

export default async function AdminReferralsPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const page = Number(params.page ?? 0);
  const search = typeof params.search === "string" ? params.search : undefined;
  const status = typeof params.status === "string" ? (params.status as ReferralStatus) : undefined;

  const result = await getAllReferrals({ page, size: 20, search, status });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">Referrals</h1>
        <p className="text-muted-foreground mt-1 text-sm">
          Every member referral, showing who referred whom and whether they joined.
        </p>
      </div>

      <ReferralsFilterBar />

      <Card>
        <CardContent>
          {result.content.length === 0 ? (
            <p className="text-muted-foreground text-sm">No referrals match these filters.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Referred by</TableHead>
                  <TableHead>Referee</TableHead>
                  <TableHead>Referee email</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Referred on</TableHead>
                  <TableHead>Joined on</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {result.content.map((referral) => (
                  <TableRow key={referral.id}>
                    <TableCell className="font-medium">
                      {referral.referrerName ?? "—"}
                      {referral.referrerEmail && (
                        <span className="text-muted-foreground block text-xs">{referral.referrerEmail}</span>
                      )}
                    </TableCell>
                    <TableCell>
                      {referral.refereeFirstName} {referral.refereeLastName}
                    </TableCell>
                    <TableCell>{referral.refereeEmail}</TableCell>
                    <TableCell>
                      <Badge variant={STATUS_VARIANT[referral.status]}>{STATUS_LABEL[referral.status]}</Badge>
                    </TableCell>
                    <TableCell>{formatDate(referral.createdAt)}</TableCell>
                    <TableCell>{formatDate(referral.acceptedAt)}</TableCell>
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
