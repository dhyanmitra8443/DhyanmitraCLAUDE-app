"use client";

import { api } from "@/lib/api/client";
import type { PaymentGatewaySettings } from "@/lib/api/types";

/** Ref: SRS 16.9 - administrator-only. Blank secret fields leave the stored value unchanged. */
export function updatePaymentGatewaySettings(payload: PaymentGatewaySettings): Promise<null> {
  return api.patch<null>("/settings/payment-gateway", payload);
}
