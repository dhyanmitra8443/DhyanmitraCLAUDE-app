import { StatTile } from "@/components/dashboard/stat-tile";
import { RecentActivityCard } from "@/components/dashboard/recent-activity-card";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { getInstructorDashboard } from "@/lib/dashboard/queries";
import { formatCompactNumber } from "@/lib/format";

export const metadata = { title: "Instructor dashboard | Dhyan Mitra" };

const STATUS_VARIANT = {
  PUBLISHED: "default",
  DRAFT: "secondary",
  ARCHIVED: "outline",
} as const;

export default async function InstructorDashboardPage() {
  const dashboard = await getInstructorDashboard();
  const courses = dashboard.courseSummaries ?? [];

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Dashboard</h1>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
        <StatTile label="Assigned courses" value={formatCompactNumber(dashboard.totalAssignedCourses ?? 0)} />
        <StatTile label="Published courses" value={formatCompactNumber(dashboard.publishedCourses ?? 0)} />
        <StatTile label="Draft courses" value={formatCompactNumber(dashboard.draftCourses ?? 0)} />
        <StatTile label="Enrolled students" value={formatCompactNumber(dashboard.totalEnrolledStudents ?? 0)} />
        <StatTile label="Upcoming live classes" value={formatCompactNumber(dashboard.upcomingLiveClasses ?? 0)} />
        <StatTile label="Completed live classes" value={formatCompactNumber(dashboard.completedLiveClasses ?? 0)} />
        <StatTile
          label="Certificates issued"
          value={formatCompactNumber(dashboard.certificatesIssuedForAssignedCourses ?? 0)}
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Your courses</CardTitle>
        </CardHeader>
        <CardContent>
          {courses.length === 0 ? (
            <p className="text-muted-foreground text-sm">No courses assigned yet.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Course</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Students</TableHead>
                  <TableHead>Active students</TableHead>
                  <TableHead>Next live class</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {courses.map((course) => (
                  <TableRow key={course.courseId}>
                    <TableCell className="font-medium">{course.courseName}</TableCell>
                    <TableCell>
                      {course.courseStatus && (
                        <Badge variant={STATUS_VARIANT[course.courseStatus]}>{course.courseStatus}</Badge>
                      )}
                    </TableCell>
                    <TableCell>{formatCompactNumber(course.totalStudents ?? 0)}</TableCell>
                    <TableCell>{formatCompactNumber(course.activeStudents ?? 0)}</TableCell>
                    <TableCell>
                      {course.nextLiveClassAt ? new Date(course.nextLiveClassAt).toLocaleString() : "—"}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <RecentActivityCard activities={dashboard.recentActivities ?? []} />
    </div>
  );
}
