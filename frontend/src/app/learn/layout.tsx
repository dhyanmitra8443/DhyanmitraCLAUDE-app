import { AppShell } from "@/components/layout/app-shell";
import { getCurrentUser } from "@/lib/users/queries";

const NAV_ITEMS = [
  { href: "/learn", label: "Dashboard" },
  { href: "/courses", label: "Courses" },
  { href: "/learn/live-classes", label: "Live Classes" },
  { href: "/learn/certificates", label: "Certificates" },
  { href: "/learn/subscriptions", label: "Subscriptions" },
  { href: "/learn/payments", label: "Payments" },
  { href: "/learn/reports", label: "Reports" },
  { href: "/learn/notifications", label: "Notifications" },
  { href: "/learn/profile", label: "Profile" },
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
