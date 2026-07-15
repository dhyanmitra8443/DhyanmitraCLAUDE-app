import "server-only";

import { fetchFromBackend } from "@/lib/api/server";
import type { PaymentGatewaySettingsView } from "@/lib/api/types";

/**
 * Ref: SRS Chapter 16 - System Settings. Only the paymentGateway slice is
 * used today (Ch.10 needs an admin UI to configure Razorpay keys); the rest
 * of this response (general/authentication/email/etc.) is for a future Ch.16
 * pass and deliberately left untyped here.
 */
export async function getPaymentGatewaySettings(): Promise<PaymentGatewaySettingsView> {
  const settings = await fetchFromBackend<{ paymentGateway: PaymentGatewaySettingsView }>("/api/v1/settings");
  return settings.paymentGateway;
}
