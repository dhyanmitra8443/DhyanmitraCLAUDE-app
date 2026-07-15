import { Card, CardContent } from "@/components/ui/card";

export function StatTile({ label, value }: { label: string; value: string | number }) {
  return (
    <Card size="sm">
      <CardContent>
        <p className="text-muted-foreground text-sm">{label}</p>
        <p className="mt-1 text-2xl font-semibold">{value}</p>
      </CardContent>
    </Card>
  );
}
