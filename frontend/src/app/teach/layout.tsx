import { AppShell } from "@/components/layout/app-shell";
import { getCurrentUser } from "@/lib/users/queries";

const NAV_ITEMS = [
  { href: "/teach", label: "Dashboard" },
  { href: "/teach/courses", label: "Courses" },
  { href: "/teach/live-classes", label: "Live Classes" },
  { href: "/teach/profile", label: "Profile" },
];

export default async function TeachLayout({ children }: { children: React.ReactNode }) {
  const user = await getCurrentUser();

  return (
    <AppShell
      areaLabel="Instructor"
      navItems={NAV_ITEMS}
      user={{ firstName: user.firstName ?? "", lastName: user.lastName ?? "", email: user.email ?? "" }}
    >
      {children}
    </AppShell>
  );
}
