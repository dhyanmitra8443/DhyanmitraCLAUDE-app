"use client";

import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { SubscriptionPlanForm } from "./subscription-plan-form";
import { SubscriptionPlanStatusAction } from "./subscription-plan-status-action";
import { formatCurrency } from "@/lib/format";
import type { SubscriptionPlanSummary } from "@/lib/api/types";

const STATUS_VARIANT = {
  ACTIVE: "default",
  INACTIVE: "secondary",
  ARCHIVED: "outline",
} as const;

/** Ref: SRS 9.3, 9.15 - administrator-only plan management for a course. */
export function SubscriptionPlansManager({
  courseId,
  plans,
}: {
  courseId: string;
  plans: SubscriptionPlanSummary[];
}) {
  const [editingId, setEditingId] = useState<string | null>(null);

  return (
    <div className="space-y-3">
      {plans.length === 0 ? (
        <p className="text-muted-foreground text-sm">No subscription plans yet.</p>
      ) : (
        <ul className="space-y-2">
          {plans.map((plan) =>
            plan.id === editingId ? (
              <li key={plan.id}>
                <SubscriptionPlanForm
                  courseId={courseId}
                  mode="edit"
                  plan={plan}
                  onDone={() => setEditingId(null)}
                />
              </li>
            ) : (
              <li key={plan.id} className="flex items-center gap-2 rounded-lg border px-3 py-2">
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-medium">{plan.planName}</span>
                    {plan.status && <Badge variant={STATUS_VARIANT[plan.status]}>{plan.status}</Badge>}
                  </div>
                  <p className="text-muted-foreground text-xs">
                    {plan.price != null ? formatCurrency(plan.price) : ""} / {plan.duration} {plan.durationUnit?.toLowerCase()}
                    {(plan.duration ?? 0) > 1 ? "s" : ""}
                  </p>
                </div>
                <Button size="sm" variant="outline" onClick={() => setEditingId(plan.id ?? null)}>
                  Edit
                </Button>
                {plan.id && plan.status && <SubscriptionPlanStatusAction planId={plan.id} status={plan.status} />}
              </li>
            ),
          )}
        </ul>
      )}

      <SubscriptionPlanForm courseId={courseId} mode="create" />
    </div>
  );
}
