package org.walkwithgod.report;

import java.util.List;

/**
 * A generated report: title, column headers, and rows of stringified values.
 */
public record ReportResult(
        String title,
        String subtitle,
        List<String> columns,
        List<List<String>> rows) {
}