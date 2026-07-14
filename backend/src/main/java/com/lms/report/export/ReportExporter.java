package com.lms.report.export;

import com.lms.report.ReportKey;
import com.lms.report.entity.ReportFormat;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Ref: SRS 15.9 - renders report rows into PDF, XLSX or CSV.
 *
 * Headers come from the ReportKey's column definitions, not from the rows,
 * so an export of an empty result set is still a valid file with correct
 * headers rather than a zero-byte download.
 */
@Component
public class ReportExporter {

    private static final DateTimeFormatter GENERATED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] export(ReportKey key, List<Map<String, Object>> rows, ReportFormat format) {
        return switch (format) {
            case CSV -> toCsv(key, rows);
            case XLSX -> toXlsx(key, rows);
            case PDF -> toPdf(key, rows);
        };
    }

    // ---------------------------------------------------------------- CSV

    private byte[] toCsv(ReportKey key, List<Map<String, Object>> rows) {
        StringBuilder csv = new StringBuilder();
        csv.append(key.columns().stream().map(c -> escapeCsv(c.label())).reduce((a, b) -> a + "," + b).orElse(""))
                .append('\n');

        for (Map<String, Object> row : rows) {
            csv.append(key.columns().stream()
                    .map(column -> escapeCsv(stringify(row.get(column.key()))))
                    .reduce((a, b) -> a + "," + b)
                    .orElse(""))
                    .append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * RFC 4180 quoting. A value containing a comma, quote or newline must be
     * quoted and its quotes doubled, otherwise a single course title with a
     * comma in it silently shifts every later column of that row.
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    // --------------------------------------------------------------- XLSX

    private byte[] toXlsx(ReportKey key, List<Map<String, Object>> rows) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(key.title());

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            List<ReportKey.Column> columns = key.columns();
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns.get(i).label());
                cell.setCellStyle(headerStyle);
            }

            for (int r = 0; r < rows.size(); r++) {
                Row sheetRow = sheet.createRow(r + 1);
                Map<String, Object> row = rows.get(r);
                for (int c = 0; c < columns.size(); c++) {
                    Object value = row.get(columns.get(c).key());
                    Cell cell = sheetRow.createCell(c);
                    // Numbers stay numbers so the spreadsheet can total them;
                    // everything else is rendered as text.
                    if (value instanceof Number number) {
                        cell.setCellValue(number.doubleValue());
                    } else {
                        cell.setCellValue(stringify(value));
                    }
                }
            }

            for (int i = 0; i < columns.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write XLSX report", e);
        }
    }

    // ---------------------------------------------------------------- PDF

    private byte[] toPdf(ReportKey key, List<Map<String, Object>> rows) {
        PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDFont boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        // Landscape: these reports are wide (up to 8 columns) and portrait
        // would force unreadable column widths.
        PDRectangle pageSize = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
        float margin = 30f;
        float fontSize = 8f;
        float rowHeight = 14f;
        float usableWidth = pageSize.getWidth() - 2 * margin;
        float columnWidth = usableWidth / key.columns().size();

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int rowIndex = 0;
            while (true) {
                PDPage page = new PDPage(pageSize);
                document.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                    float y = pageSize.getHeight() - margin;

                    // Title + generation timestamp, on every page.
                    drawText(cs, key.title(), boldFont, 14f, margin, y);
                    y -= 16f;
                    drawText(cs, "Generated " + LocalDateTime.now().format(GENERATED_AT_FORMAT)
                            + "  |  " + rows.size() + " row(s)", font, 8f, margin, y);
                    y -= 20f;

                    for (int c = 0; c < key.columns().size(); c++) {
                        drawText(cs, fit(key.columns().get(c).label(), boldFont, fontSize, columnWidth),
                                boldFont, fontSize, margin + c * columnWidth, y);
                    }
                    y -= rowHeight;

                    while (rowIndex < rows.size() && y > margin) {
                        Map<String, Object> row = rows.get(rowIndex);
                        for (int c = 0; c < key.columns().size(); c++) {
                            String value = stringify(row.get(key.columns().get(c).key()));
                            drawText(cs, fit(value, font, fontSize, columnWidth), font, fontSize,
                                    margin + c * columnWidth, y);
                        }
                        y -= rowHeight;
                        rowIndex++;
                    }
                }

                if (rowIndex >= rows.size()) {
                    break;
                }
            }

            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write PDF report", e);
        }
    }

    private void drawText(PDPageContentStream cs, String text, PDFont font, float size, float x, float y) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    /**
     * Truncates a value to the column width, with an ellipsis. Also strips
     * characters the Standard-14 fonts cannot encode (they throw rather than
     * substituting), which would otherwise fail the whole export because one
     * student's name contains a non-Latin-1 character.
     */
    private String fit(String text, PDFont font, float fontSize, float maxWidth) {
        String safe = text == null ? "" : text.replaceAll("[^\\x20-\\xFF]", "?");
        float padding = 4f;
        try {
            if (font.getStringWidth(safe) / 1000 * fontSize <= maxWidth - padding) {
                return safe;
            }
            String ellipsis = "...";
            StringBuilder truncated = new StringBuilder();
            for (char c : safe.toCharArray()) {
                String candidate = truncated.toString() + c + ellipsis;
                if (font.getStringWidth(candidate) / 1000 * fontSize > maxWidth - padding) {
                    break;
                }
                truncated.append(c);
            }
            return truncated + ellipsis;
        } catch (IOException e) {
            return safe;
        }
    }

    private String stringify(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).reduce((a, b) -> a + ", " + b).orElse("");
        }
        return String.valueOf(value);
    }
}
