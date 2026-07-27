import { AppShell } from "@/components/layout/app-shell";
import { getCurrentUser } from "@/lib/users/queries";

const NAV_ITEMS = [
  { href: "/admin", label: "Dashboard", icon: "dashboard", primary: true },
  { href: "/admin/users", label: "Users", icon: "users", primary: true },
  { href: "/admin/courses", label: "Courses", icon: "courses", primary: true },
  { href: "/admin/live-classes", label: "Live Classes", icon: "live", primary: true },
  { href: "/admin/categories", label: "Categories", icon: "categories" },
  { href: "/admin/certificates", label: "Certificates", icon: "certificates" },
  { href: "/admin/subscriptions", label: "Subscriptions", icon: "subscriptions" },
  { href: "/admin/orders", label: "Orders", icon: "orders" },
  { href: "/admin/payments", label: "Payments", icon: "payments" },
  { href: "/admin/referrals", label: "Referrals", icon: "referrals" },
  { href: "/admin/reports", label: "Reports", icon: "reports" },
  { href: "/admin/notifications", label: "Notifications", icon: "notifications" },
  { href: "/admin/notifications/log", label: "Notification Log", icon: "notification-log" },
  { href: "/admin/settings", label: "Settings", icon: "settings" },
  { href: "/admin/profile", label: "Profile", icon: "profile" },
];

export default async function AdminLayout({ children }: { children: React.ReactNode }) {
  const user = await getCurrentUser();

  return (
    <AppShell
      areaLabel="Administrator"
      navItems={NAV_ITEMS}
      notificationsHref="/admin/notifications"
      user={{ firstName: user.firstName ?? "", lastName: user.lastName ?? "", email: user.email ?? "" }}
    >
      {children}
    </AppShell>
  );
}
