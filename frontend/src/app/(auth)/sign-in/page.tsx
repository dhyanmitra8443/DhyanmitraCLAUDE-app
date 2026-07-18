import Link from "next/link";
import { Suspense } from "react";
import { SignInForm } from "./sign-in-form";

export const metadata = { title: "Sign in | Dhyan Mitra" };

export default function SignInPage() {
  return (
    <div className="mx-auto flex min-h-[70vh] w-full max-w-md flex-col justify-center px-6 py-12">
      <div className="mb-8 text-center">
        <h1 className="text-2xl font-semibold tracking-tight">Welcome back</h1>
        <p className="text-muted-foreground mt-2 text-sm">Sign in to continue your practice.</p>
      </div>

      {/* useSearchParams() (for the ?next= redirect) requires a Suspense boundary. */}
      <Suspense>
        <SignInForm />
      </Suspense>

      <p className="text-muted-foreground mt-6 text-center text-sm">
        New to Dhyan Mitra?{" "}
        <Link href="/register" className="text-foreground font-medium underline underline-offset-4">
          Create an account
        </Link>
      </p>
    </div>
  );
}
