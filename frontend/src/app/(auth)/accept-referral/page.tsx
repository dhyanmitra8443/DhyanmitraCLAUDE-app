import { Suspense } from "react";
import { AcceptReferralForm } from "./accept-referral-form";

export const metadata = { title: "Join Dhyan Mitra | Dhyan Mitra" };

export default function AcceptReferralPage() {
  return (
    <div className="mx-auto flex min-h-[70vh] w-full max-w-md flex-col justify-center px-6 py-12">
      <div className="mb-8 text-center">
        <h1 className="text-2xl font-semibold tracking-tight">Create your account</h1>
        <p className="text-muted-foreground mt-2 text-sm">Set a password to finish joining Dhyan Mitra.</p>
      </div>

      {/* useSearchParams() (for the referral token) requires a Suspense boundary. */}
      <Suspense>
        <AcceptReferralForm />
      </Suspense>
    </div>
  );
}
