/** Auto-compact number formatting for stat tiles (1,284 / 12.9K / 4.2M). */
export function formatCompactNumber(value: number): string {
  return new Intl.NumberFormat("en-IN", { notation: "compact", maximumFractionDigits: 1 }).format(value);
}

/** Ref: SRS 10 - Razorpay/INR is the only payment currency this LMS supports. */
export function formatCurrency(value: number): string {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    notation: "compact",
    maximumFractionDigits: 1,
  }).format(value);
}
