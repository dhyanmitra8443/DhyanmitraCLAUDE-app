import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { PaymentGatewayForm } from "@/components/settings/payment-gateway-form";
import { AuthenticationSettingsForm } from "@/components/settings/authentication-settings-form";
import { EmailSettingsForm } from "@/components/settings/email-settings-form";
import { NotificationSettingsForm } from "@/components/settings/notification-settings-form";
import { CertificateSettingsForm } from "@/components/settings/certificate-settings-form";
import { MaintenanceModeToggle } from "@/components/settings/maintenance-mode-toggle";
import { getSystemSettings } from "@/lib/settings/queries";

export const metadata = { title: "Settings | Dhyan Mitra" };

/**
 * Ref: SRS Chapter 16 - System Settings. General/file-upload/live-class/backup
 * configuration aren't built yet - this covers the sections most likely to
 * need day-to-day admin changes (auth policy, SMTP, notifications, payment
 * gateway, certificate branding, maintenance mode).
 */
export default async function AdminSettingsPage() {
  const settings = await getSystemSettings();

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Settings</h1>

      <Card>
        <CardHeader>
          <CardTitle>Maintenance mode</CardTitle>
        </CardHeader>
        <CardContent>
          <MaintenanceModeToggle enabled={settings.maintenanceModeEnabled ?? false} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Authentication</CardTitle>
        </CardHeader>
        <CardContent>
          <AuthenticationSettingsForm settings={settings.authentication ?? {}} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Email (SMTP)</CardTitle>
        </CardHeader>
        <CardContent>
          <EmailSettingsForm settings={settings.email ?? {}} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Notifications</CardTitle>
        </CardHeader>
        <CardContent>
          <NotificationSettingsForm settings={settings.notifications ?? {}} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Payment gateway</CardTitle>
        </CardHeader>
        <CardContent>
          <PaymentGatewayForm settings={settings.paymentGateway ?? {}} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Certificate branding</CardTitle>
        </CardHeader>
        <CardContent>
          <CertificateSettingsForm settings={settings.certificate ?? {}} />
        </CardContent>
      </Card>
    </div>
  );
}
