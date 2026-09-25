package org.walkwithgod.report;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports-builder")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final AdminReportService service;

    public AdminReportController(AdminReportService service) {
        this.service = service;
    }

    /** Preview — returns the first N rows as JSON. */
    @GetMapping("/preview")
    public ReportPreview preview(
            @RequestParam ReportType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String search) {
        ReportFilter filter = buildFilter(from, to, search);
        ReportResult result = service.generate(type, filter);

        // Cap preview rows at 50
        var cappedRows = result.rows().size() > 50
                ? result.rows().subList(0, 50)
                : result.rows();

        return new ReportPreview(
                result.title(),
                result.subtitle(),
                result.columns(),
                cappedRows,
                result.rows().size());
    }

    /** Export — streams the file back with proper headers. */
    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> export(
            @RequestParam ReportType type,
            @RequestParam(defaultValue = "CSV") String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String search) {
        ExportFormat fmt = ExportFormat.fromString(format);
        ReportFilter filter = buildFilter(from, to, search);

        byte[] bytes = service.export(type, filter, fmt);

        String filename = "wwg-" + type.name().toLowerCase() + "-report-"
                + Instant.now().toString().substring(0, 10)
                + "." + fmt.extension();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(fmt.contentType()))
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    /* ---------- helpers ---------- */

    private ReportFilter buildFilter(LocalDate from, LocalDate to, String search) {
        Instant fromI = from == null ? null : from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toI = to == null ? null : to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return new ReportFilter(fromI, toI, search, null);
    }

    /** Preview response shape. */
    public record ReportPreview(
            String title,
            String subtitle,
            java.util.List<String> columns,
            java.util.List<java.util.List<String>> rows,
            int totalRows) {
    }
}