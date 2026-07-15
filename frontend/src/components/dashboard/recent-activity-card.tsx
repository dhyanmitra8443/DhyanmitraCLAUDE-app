import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import type { RecentActivity } from "@/lib/api/types";

export function RecentActivityCard({ activities }: { activities: RecentActivity[] }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Recent activity</CardTitle>
      </CardHeader>
      <CardContent>
        {activities.length === 0 ? (
          <p className="text-muted-foreground text-sm">Nothing yet.</p>
        ) : (
          <ul className="space-y-3">
            {activities.map((activity, index) => (
              <li key={index} className="flex items-start justify-between gap-4 text-sm">
                <span>{activity.message}</span>
                <span className="text-muted-foreground shrink-0">
                  {activity.occurredAt ? new Date(activity.occurredAt).toLocaleString() : ""}
                </span>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  );
}
