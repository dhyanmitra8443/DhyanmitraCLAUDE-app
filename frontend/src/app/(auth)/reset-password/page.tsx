import { Suspense } from "react";
import { ResetPasswordForm } from "./reset-password-form";

export const metadata = { title: "Reset password | Dhyan Mitra" };

export default function ResetPasswordPage() {
  return (
    <div className="mx-auto flex min-h-[70vh] w-full max-w-md flex-col justify-center px-6 py-12">
      <div className="mb-8 text-center">
        <h1 className="text-2xl font-semibold tracking-tight">Set a new password</h1>
        <p className="text-muted-foreground mt-2 text-sm">Choose a new password for your account.</p>
      </div>

      {/* useSearchParams() (for the reset token) requires a Suspense boundary. */}
      <Suspense>
        <ResetPasswordForm />
      </Suspense>
    </div>
  );
}
