"use client";

import { api } from "@/lib/api/client";
import type {
  PaymentGatewaySettings,
  AuthenticationSettings,
  EmailSettings,
  NotificationSettings,
  CertificateSettings,
} from "@/lib/api/types";

/** Ref: SRS 16.9 - administrator-only. Blank secret fields leave the stored value unchanged. */
export function updatePaymentGatewaySettings(payload: PaymentGatewaySettings): Promise<null> {
  return api.patch<null>("/settings/payment-gateway", payload);
}

/** Ref: SRS 16.5 - password length/complexity overrides are bounded server-side. */
export function updateAuthenticationSettings(payload: AuthenticationSettings): Promise<null> {
  return api.patch<null>("/settings/authentication", payload);
}

/** Ref: SRS 16.7 - a blank smtpPassword leaves the stored value unchanged. */
export function updateEmailSettings(payload: EmailSettings): Promise<null> {
  return api.patch<null>("/settings/email", payload);
}

/** Ref: SRS 16.8 - disabling a channel affects future notifications only. */
export function updateNotificationSettings(payload: NotificationSettings): Promise<null> {
  return api.patch<null>("/settings/notifications", payload);
}

/** Ref: SRS 16.11 - applies only to newly generated certificates. */
export function updateCertificateSettings(payload: CertificateSettings): Promise<null> {
  return api.patch<null>("/settings/certificate", payload);
}

/** Ref: SRS 16.12 - while enabled, only administrators may access the system. */
export function updateMaintenanceMode(enabled: boolean): Promise<null> {
  return api.patch<null>("/settings/maintenance-mode", { enabled });
}
