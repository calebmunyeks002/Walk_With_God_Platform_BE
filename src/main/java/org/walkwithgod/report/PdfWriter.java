package org.walkwithgod.report;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

public final class PdfWriter {

    private PdfWriter() {
    }

    public static byte[] write(ReportResult report) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 40);

        // Fully-qualified name to avoid collision with our own class
        com.lowagie.text.pdf.PdfWriter.getInstance(doc, out);

        doc.open();

        // Title
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(42, 23, 64));
        Paragraph title = new Paragraph(report.title(), titleFont);
        title.setAlignment(Element.ALIGN_LEFT);
        title.setSpacingAfter(6);
        doc.add(title);

        // Subtitle
        if (report.subtitle() != null && !report.subtitle().isBlank()) {
            Font subFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(120, 110, 135));
            Paragraph sub = new Paragraph(report.subtitle(), subFont);
            sub.setSpacingAfter(18);
            doc.add(sub);
        }

        // Table
        List<String> cols = report.columns();
        PdfPTable table = new PdfPTable(cols.size());
        table.setWidthPercentage(100);

        // Header row
        Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        for (String col : cols) {
            PdfPCell cell = new PdfPCell(new Phrase(col, headerFont));
            cell.setBackgroundColor(new Color(109, 34, 164));
            cell.setPadding(6);
            cell.setBorderColor(new Color(220, 210, 235));
            table.addCell(cell);
        }

        // Data rows
        Font cellFont = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(58, 42, 74));
        boolean shade = false;
        for (List<String> row : report.rows()) {
            for (int i = 0; i < cols.size(); i++) {
                String value = i < row.size() ? row.get(i) : "";
                PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, cellFont));
                cell.setPadding(5);
                cell.setBorderColor(new Color(240, 235, 244));
                if (shade)
                    cell.setBackgroundColor(new Color(250, 248, 252));
                table.addCell(cell);
            }
            shade = !shade;
        }

        doc.add(table);

        // Footer
        Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, new Color(140, 130, 149));
        Paragraph footer = new Paragraph(
                "Generated " + java.time.Instant.now() + " — " + report.rows().size() + " rows",
                footerFont);
        footer.setSpacingBefore(14);
        doc.add(footer);

        doc.close();
        return out.toByteArray();
    }
}