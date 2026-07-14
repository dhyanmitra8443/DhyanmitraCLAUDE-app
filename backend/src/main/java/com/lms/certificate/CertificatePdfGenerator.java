package com.lms.certificate;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Ref: SRS 12.10, 12.11 - fills the DYJK Dhyan Mitra certificate template
 * (src/main/resources/certificates/template.pdf) with per-certificate data.
 *
 * The template's placeholder fields are plain text EXCEPT "[Student Full
 * Name]", which is vector artwork (a script-style graphic, not selectable
 * text) - confirmed by inspecting the template with pdfplumber, which
 * extracted zero characters in that region despite the visible content.
 * All coordinates below were extracted the same way (word/curve bounding
 * boxes in the template's own coordinate space) and are specific to this
 * exact template file; they will need re-deriving if the template changes.
 *
 * Each field is "erased" with a rectangle matching the page's own ivory
 * background before the real value is drawn on top. Known limitation: the
 * template has a very faint full-page watermark pattern, and a flat-color
 * rectangle leaves an almost imperceptible seam in that pattern rather
 * than perfectly blending - acceptable for a first implementation, but a
 * pixel-perfect version would need to re-render the background layer
 * under each patch instead of covering with a flat fill.
 */
@Component
public class CertificatePdfGenerator {

    private static final float PAGE_HEIGHT = 442.11379f;
    private static final Color IVORY_BACKGROUND = new Color(0.988235294f, 0.964705882f, 0.91372549f);
    private static final Color BROWN_TEXT = new Color(0.478431372f, 0.243137254f, 0.039215686f);
    private static final DateTimeFormatter COMPLETION_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    private final PDFont regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDFont boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private final PDFont nameFont = new PDType1Font(Standard14Fonts.FontName.TIMES_ITALIC);

    public byte[] generate(CertificateFields fields) {
        try (InputStream templateStream = new ClassPathResource("certificates/template.pdf").getInputStream();
             PDDocument document = Loader.loadPDF(templateStream.readAllBytes())) {

            PDPage page = document.getPage(0);
            try (PDPageContentStream cs = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                // Student name: vector artwork region, no live text to erase around -
                // just cover the whole bounding box and draw the real name centered in it.
                eraseRegion(cs, 191.1f, 456.3f, 38.4f, 66.8f);
                drawCentered(cs, fields.studentName(), nameFont, 26, (191.1f + 456.3f) / 2, PAGE_HEIGHT - 66.8f + 8, BROWN_TEXT);

                eraseRegion(cs, 284.7f, 362.0f, 115.4f, 124.7f);
                drawCentered(cs, fields.courseName(), boldFont, 11, (284.7f + 362.0f) / 2, PAGE_HEIGHT - 124.7f, BROWN_TEXT);

                eraseRegion(cs, 155.5f, 228.4f, 173.2f, 180.7f);
                drawLeftAligned(cs, fields.completionDate().format(COMPLETION_DATE_FORMAT), boldFont, 7.5f, 155.5f, PAGE_HEIGHT - 180.7f, BROWN_TEXT);

                eraseRegion(cs, 315.2f, 394.8f, 173.2f, 180.7f);
                drawLeftAligned(cs, fields.certificateNumber(), boldFont, 7.5f, 315.2f, PAGE_HEIGHT - 180.7f, BROWN_TEXT);

                eraseRegion(cs, 483.1f, 559.2f, 173.2f, 180.7f);
                drawLeftAligned(cs, fields.verificationId(), boldFont, 7.5f, 483.1f, PAGE_HEIGHT - 180.7f, BROWN_TEXT);

                eraseRegion(cs, 47.8f, 167.6f, 257.9f, 265.4f);
                drawCentered(cs, fields.instructorName(), boldFont, 7.5f, (47.8f + 167.6f) / 2, PAGE_HEIGHT - 265.4f, BROWN_TEXT);

                eraseRegion(cs, 479.3f, 598.6f, 257.9f, 265.4f);
                drawCentered(cs, fields.founderName(), boldFont, 7.5f, (479.3f + 598.6f) / 2, PAGE_HEIGHT - 265.4f, BROWN_TEXT);

                eraseRegion(cs, 397.1f, 448.9f, 285.2f, 290.9f);
                drawLeftAligned(cs, fields.verificationId(), regularFont, 5.8f, 397.1f, PAGE_HEIGHT - 290.9f, BROWN_TEXT);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate certificate PDF", e);
        }
    }

    private void eraseRegion(PDPageContentStream cs, float x0, float x1, float top, float bottom) throws IOException {
        cs.setNonStrokingColor(IVORY_BACKGROUND);
        cs.addRect(x0, PAGE_HEIGHT - bottom, x1 - x0, bottom - top);
        cs.fill();
    }

    private void drawCentered(PDPageContentStream cs, String text, PDFont font, float size, float centerX, float baselineY, Color color) throws IOException {
        float width = font.getStringWidth(text) / 1000 * size;
        drawText(cs, text, font, size, centerX - width / 2, baselineY, color);
    }

    private void drawLeftAligned(PDPageContentStream cs, String text, PDFont font, float size, float x, float baselineY, Color color) throws IOException {
        drawText(cs, text, font, size, x, baselineY, color);
    }

    private void drawText(PDPageContentStream cs, String text, PDFont font, float size, float x, float y, Color color) throws IOException {
        cs.setNonStrokingColor(color);
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    public record CertificateFields(
            String studentName,
            String courseName,
            LocalDate completionDate,
            String certificateNumber,
            String verificationId,
            String instructorName,
            String founderName
    ) {
        public static String joinInstructorNames(List<String> names) {
            return String.join(", ", names);
        }
    }
}
