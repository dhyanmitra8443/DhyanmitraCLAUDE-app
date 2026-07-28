"use client";

import { useState, type FormEvent } from "react";
import { SITE } from "@/lib/site";

// There is no newsletter backend yet, so this composes a real mailto to the
// institute rather than pretending to store a subscription. Honest and
// functional: submitting opens the visitor's mail client pre-addressed.
export function NewsletterForm() {
  const [email, setEmail] = useState("");

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!email) return;
    const subject = encodeURIComponent("Newsletter signup");
    const body = encodeURIComponent(`Please add me to your updates: ${email}`);
    window.location.href = `mailto:${SITE.email}?subject=${subject}&body=${body}`;
  }

  return (
    <form onSubmit={handleSubmit} className="mt-3 flex gap-2">
      <label htmlFor="newsletter-email" className="sr-only">
        Email address
      </label>
      <input
        id="newsletter-email"
        type="email"
        required
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="Enter your email"
        className="w-full rounded-md border border-white/25 bg-white/10 px-3 py-2 text-sm text-white placeholder:text-white/50 focus:border-white/50 focus:outline-none"
      />
      <button
        type="submit"
        className="bg-(--brand-orange-strong) hover:bg-(--brand-orange-strong)/90 shrink-0 rounded-md px-3 py-2 text-sm font-medium text-white"
      >
        Subscribe
      </button>
    </form>
  );
}
