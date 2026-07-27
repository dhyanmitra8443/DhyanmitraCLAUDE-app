import { AppShell } from "@/components/layout/app-shell";
import { getCurrentUser } from "@/lib/users/queries";

const NAV_ITEMS = [
  { href: "/learn", label: "Dashboard", icon: "dashboard", primary: true },
  { href: "/courses", label: "Courses", icon: "courses", primary: true },
  { href: "/learn/live-classes", label: "Live Classes", icon: "live", primary: true },
  { href: "/learn/profile", label: "Profile", icon: "profile", primary: true },
  { href: "/learn/certificates", label: "Certificates", icon: "certificates" },
  { href: "/learn/subscriptions", label: "Subscriptions", icon: "subscriptions" },
  { href: "/learn/payments", label: "Payments", icon: "payments" },
  { href: "/learn/referrals", label: "Refer", icon: "refer" },
  { href: "/learn/reports", label: "Reports", icon: "reports" },
  { href: "/learn/notifications", label: "Notifications", icon: "notifications" },
];

export default async function LearnLayout({ children }: { children: React.ReactNode }) {
  const user = await getCurrentUser();

  return (
    <AppShell
      areaLabel="Student"
      navItems={NAV_ITEMS}
      notificationsHref="/learn/notifications"
      user={{ firstName: user.firstName ?? "", lastName: user.lastName ?? "", email: user.email ?? "" }}
    >
      {children}
    </AppShell>
  );
}
