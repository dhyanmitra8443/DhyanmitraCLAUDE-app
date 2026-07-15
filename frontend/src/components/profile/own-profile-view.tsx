import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { PhotoUploader } from "./photo-uploader";
import { ProfileForm } from "./profile-form";
import { ChangePasswordForm } from "./change-password-form";
import type { UserProfile } from "@/lib/api/types";

export function OwnProfileView({ profile }: { profile: UserProfile }) {
  return (
    <div className="max-w-2xl space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Profile</h1>

      <Card>
        <CardContent>
          <PhotoUploader
            firstName={profile.firstName ?? ""}
            lastName={profile.lastName ?? ""}
            photoUrl={profile.profilePhotoUrl}
          />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Personal information</CardTitle>
        </CardHeader>
        <CardContent>
          <ProfileForm profile={profile} mode="own" />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Change password</CardTitle>
        </CardHeader>
        <CardContent>
          <ChangePasswordForm />
        </CardContent>
      </Card>
    </div>
  );
}
