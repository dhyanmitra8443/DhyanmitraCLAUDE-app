import { AppShell } from "@/components/layout/app-shell";
import { getCurrentUser } from "@/lib/users/queries";

const NAV_ITEMS = [
  { href: "/teach", label: "Dashboard", icon: "dashboard", primary: true },
  { href: "/teach/courses", label: "Courses", icon: "courses", primary: true },
  { href: "/teach/live-classes", label: "Live Classes", icon: "live", primary: true },
  { href: "/teach/profile", label: "Profile", icon: "profile", primary: true },
  { href: "/teach/referrals", label: "Refer", icon: "refer" },
  { href: "/teach/reports", label: "Reports", icon: "reports" },
  { href: "/teach/notifications", label: "Notifications", icon: "notifications" },
];

export default async function TeachLayout({ children }: { children: React.ReactNode }) {
  const user = await getCurrentUser();

  return (
    <AppShell
      areaLabel="Instructor"
      navItems={NAV_ITEMS}
      notificationsHref="/teach/notifications"
      user={{ firstName: user.firstName ?? "", lastName: user.lastName ?? "", email: user.email ?? "" }}
    >
      {children}
    </AppShell>
  );
}
