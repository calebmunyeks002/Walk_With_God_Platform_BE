package org.walkwithgod.report;

import java.time.Instant;

public record ReportFilter(
        Instant from,
        Instant to,
        String search,
        String extra // free-form filter used by specific reports
) {
    public static ReportFilter empty() {
        return new ReportFilter(null, null, null, null);
    }
}