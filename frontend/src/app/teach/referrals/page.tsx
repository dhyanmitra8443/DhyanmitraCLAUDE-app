import { MyReferralsView } from "@/components/referrals/my-referrals-view";
import { getMyReferrals } from "@/lib/referrals/queries";

export const metadata = { title: "Referrals | Dhyan Mitra" };

export default async function TeachReferralsPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const params = await searchParams;
  const page = Number(params.page ?? 0);
  const result = await getMyReferrals({ page, size: 20 });
  return <MyReferralsView result={result} />;
}
