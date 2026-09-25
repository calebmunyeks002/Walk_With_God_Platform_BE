package org.walkwithgod.report;

public enum ExportFormat {
    CSV,
    XLSX,
    PDF;

    public static ExportFormat fromString(String s) {
        if (s == null) return CSV;
        try {
            return ExportFormat.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CSV;
        }
    }

    public String contentType() {
        return switch (this) {
            case CSV -> "text/csv";
            case XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case PDF -> "application/pdf";
        };
    }

    public String extension() {
        return switch (this) {
            case CSV -> "csv";
            case XLSX -> "xlsx";
            case PDF -> "pdf";
        };
    }
}