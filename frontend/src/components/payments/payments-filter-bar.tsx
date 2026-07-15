"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

const selectClassName =
  "h-8 rounded-lg border border-input bg-transparent px-2 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30";

/** Ref: SRS 10.16 - search by transaction reference, status. */
export function PaymentsFilterBar() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [transactionReference, setTransactionReference] = useState(searchParams.get("transactionReference") ?? "");

  function updateParams(updates: Record<string, string | null>) {
    const params = new URLSearchParams(searchParams.toString());
    for (const [key, value] of Object.entries(updates)) {
      if (value) params.set(key, value);
      else params.delete(key);
    }
    params.delete("page");
    router.push(`/admin/payments?${params.toString()}`);
  }

  return (
    <form
      className="flex flex-wrap items-center gap-2"
      onSubmit={(e) => {
        e.preventDefault();
        updateParams({ transactionReference: transactionReference || null });
      }}
    >
      <Input
        value={transactionReference}
        onChange={(e) => setTransactionReference(e.target.value)}
        placeholder="Transaction reference"
        className="max-w-xs"
      />
      <Button type="submit" variant="outline" size="sm">
        Search
      </Button>

      <select
        className={selectClassName}
        value={searchParams.get("status") ?? ""}
        onChange={(e) => updateParams({ status: e.target.value || null })}
      >
        <option value="">All statuses</option>
        <option value="PENDING">PENDING</option>
        <option value="SUCCESS">SUCCESS</option>
        <option value="FAILED">FAILED</option>
        <option value="CANCELLED">CANCELLED</option>
      </select>
    </form>
  );
}
