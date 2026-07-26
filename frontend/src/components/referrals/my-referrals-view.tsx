import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { PaginationControls } from "@/components/shared/pagination-controls";
import { ReferDialog } from "@/components/referrals/refer-dialog";
import type { Paginated, ReferralStatus, ReferralSummary } from "@/lib/api/types";

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

/**
 * The referrer's own "who have I referred" view, shared by the student and
 * instructor portals - the backend endpoint and permissions are identical for
 * both roles.
 */
export function MyReferralsView({ result }: { result: Paginated<ReferralSummary> }) {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">Refer &amp; earn</h1>
          <p className="text-muted-foreground mt-1 text-sm">
            Invite friends to Dhyan Mitra and track who has joined.
          </p>
        </div>
        <ReferDialog />
      </div>

      <Card>
        <CardContent>
          {result.content.length === 0 ? (
            <p className="text-muted-foreground text-sm">
              You haven&apos;t referred anyone yet. Use “Refer someone” to send your first invitation.
            </p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Referred on</TableHead>
                  <TableHead>Joined on</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {result.content.map((referral) => (
                  <TableRow key={referral.id}>
                    <TableCell className="font-medium">
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
