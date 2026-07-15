import { StatTile } from "@/components/dashboard/stat-tile";
import { RecentActivityCard } from "@/components/dashboard/recent-activity-card";
import { getAdminDashboard } from "@/lib/dashboard/queries";
import { formatCompactNumber, formatCurrency } from "@/lib/format";

export const metadata = { title: "Admin dashboard | Dhyan Mitra" };

export default async function AdminDashboardPage() {
  const dashboard = await getAdminDashboard();

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Dashboard</h1>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
        <StatTile label="Registered students" value={formatCompactNumber(dashboard.totalRegisteredStudents ?? 0)} />
        <StatTile label="Active students" value={formatCompactNumber(dashboard.totalActiveStudents ?? 0)} />
        <StatTile label="Instructors" value={formatCompactNumber(dashboard.totalInstructors ?? 0)} />
        <StatTile label="Total courses" value={formatCompactNumber(dashboard.totalCourses ?? 0)} />
        <StatTile label="Published courses" value={formatCompactNumber(dashboard.publishedCourses ?? 0)} />
        <StatTile label="Draft courses" value={formatCompactNumber(dashboard.draftCourses ?? 0)} />
        <StatTile label="Archived courses" value={formatCompactNumber(dashboard.archivedCourses ?? 0)} />
        <StatTile label="Categories" value={formatCompactNumber(dashboard.totalCategories ?? 0)} />
        <StatTile label="Live classes" value={formatCompactNumber(dashboard.totalLiveClasses ?? 0)} />
        <StatTile label="Scheduled live classes" value={formatCompactNumber(dashboard.scheduledLiveClasses ?? 0)} />
        <StatTile label="Completed live classes" value={formatCompactNumber(dashboard.completedLiveClasses ?? 0)} />
        <StatTile label="Subscription plans" value={formatCompactNumber(dashboard.totalSubscriptionPlans ?? 0)} />
        <StatTile label="Active subscriptions" value={formatCompactNumber(dashboard.activeSubscriptions ?? 0)} />
        <StatTile label="Expired subscriptions" value={formatCompactNumber(dashboard.expiredSubscriptions ?? 0)} />
        <StatTile label="Total orders" value={formatCompactNumber(dashboard.totalOrders ?? 0)} />
        <StatTile label="Successful payments" value={formatCompactNumber(dashboard.successfulPayments ?? 0)} />
        <StatTile label="Failed payments" value={formatCompactNumber(dashboard.failedPayments ?? 0)} />
        <StatTile label="Total revenue" value={formatCurrency(dashboard.totalRevenue ?? 0)} />
        <StatTile label="Certificates issued" value={formatCompactNumber(dashboard.certificatesIssued ?? 0)} />
      </div>

      <RecentActivityCard activities={dashboard.recentActivities ?? []} />
    </div>
  );
}
