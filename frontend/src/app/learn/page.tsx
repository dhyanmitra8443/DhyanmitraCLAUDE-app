import { StatTile } from "@/components/dashboard/stat-tile";
import { RecentActivityCard } from "@/components/dashboard/recent-activity-card";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress, ProgressTrack, ProgressIndicator } from "@/components/ui/progress";
import { getStudentDashboard } from "@/lib/dashboard/queries";
import { formatCompactNumber } from "@/lib/format";

export const metadata = { title: "Dashboard | Dhyan Mitra" };

export default async function StudentDashboardPage() {
  const dashboard = await getStudentDashboard();
  const myCourses = dashboard.myCourses ?? [];
  const upcomingLiveClasses = dashboard.upcomingLiveClasses ?? [];
  const certificates = dashboard.certificates ?? [];

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Dashboard</h1>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
        <StatTile label="Active courses" value={formatCompactNumber(dashboard.activeCourses ?? 0)} />
        <StatTile label="Completed courses" value={formatCompactNumber(dashboard.completedCourses ?? 0)} />
        <StatTile
          label="Overall progress"
          value={`${Math.round(dashboard.overallLearningProgress ?? 0)}%`}
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>My courses</CardTitle>
        </CardHeader>
        <CardContent>
          {myCourses.length === 0 ? (
            <p className="text-muted-foreground text-sm">You haven&apos;t enrolled in any courses yet.</p>
          ) : (
            <ul className="space-y-4">
              {myCourses.map((course) => (
                <li key={course.id} className="space-y-1.5">
                  <div className="flex items-center justify-between gap-4">
                    <span className="font-medium">{course.title}</span>
                    <span className="text-muted-foreground text-sm tabular-nums">
                      {Math.round(course.progressPercentage ?? 0)}%
                    </span>
                  </div>
                  <Progress value={course.progressPercentage ?? 0}>
                    <ProgressTrack>
                      <ProgressIndicator />
                    </ProgressTrack>
                  </Progress>
                  {course.subscriptionExpiryDate && (
                    <p className="text-muted-foreground text-xs">
                      Access until {new Date(course.subscriptionExpiryDate).toLocaleDateString()}
                    </p>
                  )}
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Upcoming live classes</CardTitle>
          </CardHeader>
          <CardContent>
            {upcomingLiveClasses.length === 0 ? (
              <p className="text-muted-foreground text-sm">No live classes scheduled.</p>
            ) : (
              <ul className="space-y-3">
                {upcomingLiveClasses.map((liveClass) => (
                  <li key={liveClass.id} className="flex items-center justify-between gap-4 text-sm">
                    <span className="font-medium">{liveClass.title}</span>
                    <span className="text-muted-foreground">
                      {liveClass.scheduledDate} · {liveClass.scheduledTime}
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Certificates</CardTitle>
          </CardHeader>
          <CardContent>
            {certificates.length === 0 ? (
              <p className="text-muted-foreground text-sm">Complete a course to earn your first certificate.</p>
            ) : (
              <ul className="space-y-3">
                {certificates.map((certificate) => (
                  <li key={certificate.id} className="flex items-center justify-between gap-4 text-sm">
                    <span className="font-medium">{certificate.courseName}</span>
                    <span className="text-muted-foreground">{certificate.certificateNumber}</span>
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>
      </div>

      <RecentActivityCard activities={dashboard.recentActivities ?? []} />
    </div>
  );
}
