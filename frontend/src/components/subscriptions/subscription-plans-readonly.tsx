import { Badge } from "@/components/ui/badge";
import { formatCurrency } from "@/lib/format";
import type { SubscriptionPlanSummary } from "@/lib/api/types";

/**
 * Ref: SRS 9.18 - plan management is administrator-only; instructors get
 * read-only visibility so they know whether their course has an ACTIVE plan
 * yet (required before it can be published, Ref: SRS 9.6).
 */
export function SubscriptionPlansReadOnly({ plans }: { plans: SubscriptionPlanSummary[] }) {
  if (plans.length === 0) {
    return (
      <p className="text-muted-foreground text-sm">
        No active subscription plans yet - ask an administrator to add one before publishing.
      </p>
    );
  }

  return (
    <ul className="space-y-2">
      {plans.map((plan) => (
        <li key={plan.id} className="flex items-center justify-between rounded-lg border px-3 py-2">
          <div>
            <span className="text-sm font-medium">{plan.planName}</span>
            <p className="text-muted-foreground text-xs">
              {plan.price != null ? formatCurrency(plan.price) : ""} / {plan.duration} {plan.durationUnit?.toLowerCase()}
              {(plan.duration ?? 0) > 1 ? "s" : ""}
            </p>
          </div>
          <Badge>ACTIVE</Badge>
        </li>
      ))}
    </ul>
  );
}
