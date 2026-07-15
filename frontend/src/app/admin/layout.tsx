import { AppShell } from "@/components/layout/app-shell";
import { getCurrentUser } from "@/lib/users/queries";

const NAV_ITEMS = [
  { href: "/admin", label: "Dashboard" },
  { href: "/admin/users", label: "Users" },
  { href: "/admin/courses", label: "Courses" },
  { href: "/admin/categories", label: "Categories" },
  { href: "/admin/live-classes", label: "Live Classes" },
  { href: "/admin/certificates", label: "Certificates" },
  { href: "/admin/subscriptions", label: "Subscriptions" },
  { href: "/admin/orders", label: "Orders" },
  { href: "/admin/payments", label: "Payments" },
  { href: "/admin/settings", label: "Settings" },
  { href: "/admin/profile", label: "Profile" },
];

export default async function AdminLayout({ children }: { children: React.ReactNode }) {
  const user = await getCurrentUser();

  return (
    <AppShell
      areaLabel="Administrator"
      navItems={NAV_ITEMS}
      user={{ firstName: user.firstName ?? "", lastName: user.lastName ?? "", email: user.email ?? "" }}
    >
      {children}
    </AppShell>
  );
}
