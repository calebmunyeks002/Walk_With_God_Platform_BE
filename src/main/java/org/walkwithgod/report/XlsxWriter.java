package org.walkwithgod.report;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public final class XlsxWriter {

    private XlsxWriter() {
    }

    public static byte[] write(ReportResult report) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet(safeSheetName(report.title()));

            // ---------- Styles ----------

            // Title
            XSSFFont titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setColor(new XSSFColor(new java.awt.Color(42, 23, 64), null));

            XSSFCellStyle titleStyle = wb.createCellStyle();
            titleStyle.setFont(titleFont);

            // Subtitle
            XSSFFont subFont = wb.createFont();
            subFont.setFontHeightInPoints((short) 10);
            subFont.setColor(new XSSFColor(new java.awt.Color(120, 110, 135), null));

            XSSFCellStyle subStyle = wb.createCellStyle();
            subStyle.setFont(subFont);

            // Header (white on brand purple)
            XSSFFont headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            XSSFCellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(
                    new XSSFColor(new java.awt.Color(109, 34, 164), null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Zebra shading
            XSSFCellStyle zebraStyle = wb.createCellStyle();
            zebraStyle.setFillForegroundColor(
                    new XSSFColor(new java.awt.Color(250, 248, 252), null));
            zebraStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int rowIdx = 0;

            // ---------- Title ----------
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(report.title());
            titleCell.setCellStyle(titleStyle);

            // ---------- Subtitle ----------
            if (report.subtitle() != null && !report.subtitle().isBlank()) {
                Row subRow = sheet.createRow(rowIdx++);
                Cell subCell = subRow.createCell(0);
                subCell.setCellValue(report.subtitle());
                subCell.setCellStyle(subStyle);
                rowIdx++; // blank spacer row
            }

            // ---------- Header ----------
            Row headerRow = sheet.createRow(rowIdx++);
            List<String> cols = report.columns();
            for (int i = 0; i < cols.size(); i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(cols.get(i));
                c.setCellStyle(headerStyle);
            }

            // ---------- Data ----------
            boolean shade = false;
            for (List<String> row : report.rows()) {
                Row r = sheet.createRow(rowIdx++);
                for (int i = 0; i < row.size(); i++) {
                    Cell c = r.createCell(i);
                    c.setCellValue(row.get(i) == null ? "" : row.get(i));
                    if (shade)
                        c.setCellStyle(zebraStyle);
                }
                shade = !shade;
            }

            // ---------- Column widths ----------
            for (int i = 0; i < cols.size(); i++) {
                sheet.autoSizeColumn(i);
                int current = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(current + 600, 15000));
            }

            wb.write(out);
            return out.toByteArray();
        }
    }

    private static String safeSheetName(String s) {
        if (s == null)
            return "Report";
        String cleaned = s.replaceAll("[\\\\/?*\\[\\]:]", "");
        return cleaned.length() > 31 ? cleaned.substring(0, 31) : cleaned;
    }
}