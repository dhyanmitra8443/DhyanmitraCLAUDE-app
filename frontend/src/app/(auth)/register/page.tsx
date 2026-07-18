import Link from "next/link";
import { RegisterForm } from "./register-form";

export const metadata = { title: "Create account | Dhyan Mitra" };

export default function RegisterPage() {
  return (
    <div className="mx-auto flex min-h-[70vh] w-full max-w-md flex-col justify-center px-6 py-12">
      <div className="mb-8 text-center">
        <h1 className="text-2xl font-semibold tracking-tight">Create your account</h1>
        <p className="text-muted-foreground mt-2 text-sm">Start your yoga practice with Dhyan Mitra.</p>
      </div>

      <RegisterForm />

      <p className="text-muted-foreground mt-6 text-center text-sm">
        Already have an account?{" "}
        <Link href="/sign-in" className="text-foreground font-medium underline underline-offset-4">
          Sign in
        </Link>
      </p>
    </div>
  );
}
