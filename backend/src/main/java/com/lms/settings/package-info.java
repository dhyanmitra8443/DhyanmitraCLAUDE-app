/**
 * Ref: SRS Chapter 16 - System Settings (singleton configuration).
 *
 * This module is the application's live control plane, not just a CRUD
 * surface: SystemSettingsService supplies the password policy enforced by
 * PasswordConstraintValidator, the maintenance-mode flag checked by
 * MaintenanceModeFilter, the notification channel toggles honoured by
 * NotificationService, the certificate branding used at issuance, and the
 * Razorpay/SMTP credentials used by the payment and email paths (each
 * falling back to environment configuration when unset).
 *
 * Credentials are stored AES-GCM encrypted (SecretCipher) and are never
 * returned by GET /settings - only a "configured" boolean is.
 */
package com.lms.settings;
