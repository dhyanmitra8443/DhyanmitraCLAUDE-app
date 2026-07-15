import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { PaymentGatewayForm } from "@/components/settings/payment-gateway-form";
import { getPaymentGatewaySettings } from "@/lib/settings/queries";

export const metadata = { title: "Settings | Dhyan Mitra" };

/**
 * Ref: SRS Chapter 16 - System Settings. Only the Payment Gateway section is
 * built so far, since Ch.10 (Razorpay checkout) needs it to function; the
 * rest of Ch.16 (general/authentication/email/etc.) is a separate future pass.
 */
export default async function AdminSettingsPage() {
  const paymentGateway = await getPaymentGatewaySettings();

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <h1 className="text-2xl font-semibold tracking-tight">Settings</h1>

      <Card>
        <CardHeader>
          <CardTitle>Payment gateway</CardTitle>
        </CardHeader>
        <CardContent>
          <PaymentGatewayForm settings={paymentGateway} />
        </CardContent>
      </Card>
    </div>
  );
}
