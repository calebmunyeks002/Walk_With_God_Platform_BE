package org.walkwithgod.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CsvWriter {

    private CsvWriter() {
    }

    public static byte[] write(ReportResult report) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            // BOM for Excel compatibility with UTF-8
            w.write('\uFEFF');

            w.write(escape(report.title()));
            w.write('\n');
            if (report.subtitle() != null && !report.subtitle().isBlank()) {
                w.write(escape(report.subtitle()));
                w.write('\n');
            }
            w.write('\n');

            writeRow(w, report.columns());
            for (List<String> row : report.rows()) {
                writeRow(w, row);
            }
        }
        return out.toByteArray();
    }

    private static void writeRow(OutputStreamWriter w, List<String> values) throws IOException {
        for (int i = 0; i < values.size(); i++) {
            if (i > 0)
                w.write(',');
            w.write(escape(values.get(i)));
        }
        w.write('\n');
    }

    private static String escape(String v) {
        if (v == null)
            return "";
        String s = v.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}