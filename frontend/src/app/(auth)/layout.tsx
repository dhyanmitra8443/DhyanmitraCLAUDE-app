import type { ReactNode } from "react";
import { PublicShell } from "@/components/marketing/public-shell";

// All auth screens (sign-in, register, forgot/reset password, accept
// invitation) share the public brand shell with header + footer.
export default function AuthLayout({ children }: { children: ReactNode }) {
  return <PublicShell>{children}</PublicShell>;
}
