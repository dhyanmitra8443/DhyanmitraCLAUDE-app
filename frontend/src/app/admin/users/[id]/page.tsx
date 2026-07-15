import { notFound } from "next/navigation";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ProfileForm } from "@/components/profile/profile-form";
import { UserStatusAction } from "@/components/admin/user-status-action";
import { getUserFullProfile } from "@/lib/users/queries";
import { ApiError } from "@/lib/api/errors";

export const metadata = { title: "User detail | Dhyan Mitra" };

const STATUS_VARIANT = {
  ACTIVE: "default",
  INACTIVE: "secondary",
  BLOCKED: "destructive",
} as const;

export default async function AdminUserDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  let profile;
  try {
    profile = await getUserFullProfile(id);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) notFound();
    throw error;
  }

  return (
    <div className="max-w-2xl space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">
            {profile.firstName} {profile.lastName}
          </h1>
          <div className="mt-1 flex items-center gap-2">
            {profile.role && <Badge variant="outline">{profile.role}</Badge>}
            {profile.status && <Badge variant={STATUS_VARIANT[profile.status]}>{profile.status}</Badge>}
          </div>
        </div>
        {profile.status && <UserStatusAction userId={id} status={profile.status} />}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Profile</CardTitle>
        </CardHeader>
        <CardContent>
          <ProfileForm profile={profile} mode="admin" userId={id} />
        </CardContent>
      </Card>

      {profile.role === "STUDENT" && (
        <>
          <Card>
            <CardHeader>
              <CardTitle>Subscriptions</CardTitle>
            </CardHeader>
            <CardContent>
              {(profile.subscriptions ?? []).length === 0 ? (
                <p className="text-muted-foreground text-sm">No subscriptions yet.</p>
              ) : (
                <ul className="space-y-2 text-sm">
                  {profile.subscriptions!.map((subscription) => (
                    <li key={subscription.id} className="flex items-center justify-between">
                      <span>{subscription.course?.title}</span>
                      <span className="text-muted-foreground">{subscription.status}</span>
                    </li>
                  ))}
                </ul>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Certificates earned</CardTitle>
            </CardHeader>
            <CardContent>
              {(profile.certificatesEarned ?? []).length === 0 ? (
                <p className="text-muted-foreground text-sm">No certificates yet.</p>
              ) : (
                <ul className="space-y-2 text-sm">
                  {profile.certificatesEarned!.map((certificate) => (
                    <li key={certificate.id} className="flex items-center justify-between">
                      <span>{certificate.courseName}</span>
                      <span className="text-muted-foreground">{certificate.certificateNumber}</span>
                    </li>
                  ))}
                </ul>
              )}
            </CardContent>
          </Card>
        </>
      )}

      {profile.role === "INSTRUCTOR" && (
        <Card>
          <CardHeader>
            <CardTitle>Assigned courses</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <p className="text-sm">
              <span className="text-muted-foreground">Total students: </span>
              {profile.studentCount ?? 0}
            </p>
            {(profile.assignedCourses ?? []).length === 0 ? (
              <p className="text-muted-foreground text-sm">No courses assigned yet.</p>
            ) : (
              <ul className="space-y-2 text-sm">
                {profile.assignedCourses!.map((course) => (
                  <li key={course.id}>{course.title}</li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
