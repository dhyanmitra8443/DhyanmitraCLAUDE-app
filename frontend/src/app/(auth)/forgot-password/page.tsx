import Link from "next/link";
import { ForgotPasswordForm } from "./forgot-password-form";

export const metadata = { title: "Forgot password | Dhyan Mitra" };

export default function ForgotPasswordPage() {
  return (
    <div className="mx-auto flex min-h-[70vh] w-full max-w-md flex-col justify-center px-6 py-12">
      <div className="mb-8 text-center">
        <h1 className="text-2xl font-semibold tracking-tight">Reset your password</h1>
        <p className="text-muted-foreground mt-2 text-sm">
          Enter your email and we&apos;ll send you a reset link.
        </p>
      </div>

      <ForgotPasswordForm />

      <p className="text-muted-foreground mt-6 text-center text-sm">
        Remembered it?{" "}
        <Link href="/sign-in" className="text-foreground font-medium underline underline-offset-4">
          Sign in
        </Link>
      </p>
    </div>
  );
}
