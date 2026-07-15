import { Suspense } from "react";
import { AcceptInvitationForm } from "./accept-invitation-form";

export const metadata = { title: "Accept invitation | Dhyan Mitra" };

export default function AcceptInvitationPage() {
  return (
    <div className="mx-auto flex min-h-screen w-full max-w-md flex-col justify-center px-6 py-12">
      <div className="mb-8 text-center">
        <h1 className="text-2xl font-semibold tracking-tight">Activate your instructor account</h1>
        <p className="text-muted-foreground mt-2 text-sm">Set a password to finish setting up your account.</p>
      </div>

      {/* useSearchParams() (for the invitation token) requires a Suspense boundary. */}
      <Suspense>
        <AcceptInvitationForm />
      </Suspense>
    </div>
  );
}
