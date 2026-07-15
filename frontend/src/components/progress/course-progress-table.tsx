import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import type { CourseProgressSummary } from "@/lib/api/types";

const STATUS_VARIANT = {
  IN_PROGRESS: "outline",
  COMPLETED: "default",
} as const;

/** Ref: SRS 12.7 - administrator or assigned instructor view of every enrolled student's progress. */
export function CourseProgressTable({ rows }: { rows: CourseProgressSummary[] }) {
  if (rows.length === 0) {
    return <p className="text-muted-foreground text-sm">No enrolled students yet.</p>;
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Student ID</TableHead>
          <TableHead>Progress</TableHead>
          <TableHead>Lessons</TableHead>
          <TableHead>Status</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {rows.map((row) => (
          <TableRow key={row.studentId}>
            <TableCell className="text-muted-foreground font-mono text-xs">{row.studentId}</TableCell>
            <TableCell className="tabular-nums">{Math.round(row.progressPercentage ?? 0)}%</TableCell>
            <TableCell>
              {row.completedLessons ?? 0} / {row.totalPublishedLessons ?? 0}
            </TableCell>
            <TableCell>
              {row.completionStatus && <Badge variant={STATUS_VARIANT[row.completionStatus]}>{row.completionStatus}</Badge>}
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
