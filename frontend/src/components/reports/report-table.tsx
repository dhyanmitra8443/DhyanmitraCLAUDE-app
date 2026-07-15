import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import type { ReportColumn } from "@/lib/api/types";

function formatCell(value: unknown): string {
  if (value === null || value === undefined || value === "") return "—";
  if (typeof value === "boolean") return value ? "Yes" : "No";
  return String(value);
}

/** Ref: SRS 15.4-15.11 - column list and row shape both depend on reportKey, so this renders whatever the backend sends. */
export function ReportTable({ columns, rows }: { columns: ReportColumn[]; rows: Record<string, unknown>[] }) {
  if (rows.length === 0) {
    return <p className="text-muted-foreground text-sm">No data for this report and filter combination.</p>;
  }

  return (
    <div className="overflow-x-auto">
      <Table>
        <TableHeader>
          <TableRow>
            {columns.map((column) => (
              <TableHead key={column.key}>{column.label}</TableHead>
            ))}
          </TableRow>
        </TableHeader>
        <TableBody>
          {rows.map((row, index) => (
            // Ref: report rows have no stable id (Ref: ReportDataResponse -
            // arbitrary Map<String,Object> per row) - index is the only key available.
            <TableRow key={index}>
              {columns.map((column) => (
                <TableCell key={column.key}>{formatCell(row[column.key])}</TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
