import { OwnProfileView } from "@/components/profile/own-profile-view";
import { getCurrentUser } from "@/lib/users/queries";

export const metadata = { title: "Profile | Dhyan Mitra" };

export default async function StudentProfilePage() {
  const profile = await getCurrentUser();
  return <OwnProfileView profile={profile} />;
}
