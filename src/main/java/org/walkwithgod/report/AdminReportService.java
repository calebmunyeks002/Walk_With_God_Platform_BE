package org.walkwithgod.report;

import org.springframework.stereotype.Service;
import org.walkwithgod.admin.AdminService;
import org.walkwithgod.audit.AuditAction;
import org.walkwithgod.audit.AuditLog;
import org.walkwithgod.audit.AuditService;
import org.walkwithgod.community.Post;
import org.walkwithgod.devotion.Devotion;
import org.walkwithgod.mentor.MentorRequest;
import org.walkwithgod.trivia.TriviaQuestion;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminReportService {

    private static final int MAX_AUDIT_ROWS = 50_000;

    private final AdminService admin;
    private final AuditService audit;

    public AdminReportService(AdminService admin, AuditService audit) {
        this.admin = admin;
        this.audit = audit;
    }

    public ReportResult generate(ReportType type, ReportFilter filter) {
        return switch (type) {
            case USERS -> buildUsersReport(filter);
            case MENTORS -> buildMentorsReport(filter);
            case COMMUNITY -> buildCommunityReport(filter);
            case MODERATION -> buildModerationReport(filter);
            case AUDIT -> buildAuditReport(filter);
            case DEVOTIONS -> buildDevotionsReport(filter);
            case TRIVIA -> buildTriviaReport(filter);
        };
    }

    public byte[] export(ReportType type, ReportFilter filter, ExportFormat format) {
        ReportResult report = generate(type, filter);
        byte[] bytes = serialize(report, format);

        audit.record(
                AuditAction.REPORT_EXPORTED,
                "REPORT",
                null,
                "Exported " + type + " report as " + format + " (" + report.rows().size() + " rows)");

        return bytes;
    }

    private byte[] serialize(ReportResult report, ExportFormat format) {
        try {
            return switch (format) {
                case CSV -> CsvWriter.write(report);
                case XLSX -> XlsxWriter.write(report);
                case PDF -> PdfWriter.write(report);
            };
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize report: " + e.getMessage(), e);
        }
    }

    /*
     * =========================================================
     * Report builders
     * =========================================================
     */

    private ReportResult buildUsersReport(ReportFilter f) {
        var page = admin.listUsers(f.search(), null, null, 0, 10_000);
        List<List<String>> rows = new ArrayList<>();
        for (var u : page.content()) {
            if (!matchesDate(u.createdAt(), f))
                continue;
            rows.add(List.of(
                    u.id(),
                    u.name(),
                    u.email(),
                    u.role().name(),
                    String.valueOf(u.enabled()),
                    String.valueOf(u.suspended()),
                    u.suspended() ? String.valueOf(u.suspensionReason()) : "",
                    u.createdAt()));
        }
        return new ReportResult(
                "Users Report",
                buildSubtitle(f),
                List.of("ID", "Name", "Email", "Role", "Enabled", "Suspended", "Suspension Reason", "Joined"),
                rows);
    }

    private ReportResult buildMentorsReport(ReportFilter f) {
        List<MentorRequest> all = admin.allMentorRequests();
        List<List<String>> rows = new ArrayList<>();

        for (MentorRequest r : all) {
            if (!matchesDate(r.getCreatedAt().toString(), f))
                continue;

            String memberName = r.getMember() == null ? "—" : r.getMember().getName();
            String memberEmail = r.getMember() == null ? "—" : r.getMember().getEmail();
            String mentorName = r.getMentor() == null || r.getMentor().getUser() == null
                    ? "—"
                    : r.getMentor().getUser().getName();

            rows.add(List.of(
                    r.getId().toString(),
                    memberName,
                    memberEmail,
                    mentorName,
                    r.getStatus(),
                    shorten(r.getMessage(), 120),
                    r.getRespondedAt() == null ? "" : r.getRespondedAt().toString(),
                    r.getCreatedAt().toString()));
        }

        return new ReportResult(
                "Mentor Activity Report",
                buildSubtitle(f),
                List.of("Request ID", "Member", "Email", "Mentor", "Status", "Message", "Responded At", "Requested At"),
                rows);
    }

    private ReportResult buildCommunityReport(ReportFilter f) {
        List<Post> all = admin.allPosts();
        List<List<String>> rows = new ArrayList<>();

        for (Post p : all) {
            if (!matchesDate(p.getCreatedAt().toString(), f))
                continue;
            if (f.search() != null && !f.search().isBlank()) {
                String q = f.search().toLowerCase();
                if (!p.getContent().toLowerCase().contains(q))
                    continue;
            }

            String author = p.getAuthor() == null ? "—" : p.getAuthor().getName();
            String email = p.getAuthor() == null ? "—" : p.getAuthor().getEmail();

            rows.add(List.of(
                    p.getId().toString(),
                    author,
                    email,
                    shorten(p.getContent(), 100),
                    p.getScriptureReference() == null ? "" : p.getScriptureReference(),
                    String.valueOf(p.isHidden()),
                    p.getCreatedAt().toString()));
        }

        return new ReportResult(
                "Community Activity Report",
                buildSubtitle(f),
                List.of("Post ID", "Author", "Email", "Content Preview", "Scripture", "Hidden", "Posted At"),
                rows);
    }

    private ReportResult buildModerationReport(ReportFilter f) {
        List<Post> all = admin.allPosts();
        List<List<String>> rows = new ArrayList<>();

        for (Post p : all) {
            if (!p.isHidden())
                continue; // only moderated content
            if (!matchesDate(p.getCreatedAt().toString(), f))
                continue;

            String author = p.getAuthor() == null ? "—" : p.getAuthor().getName();

            rows.add(List.of(
                    p.getId().toString(),
                    author,
                    p.getAuthor() == null ? "—" : p.getAuthor().getEmail(),
                    shorten(p.getContent(), 80),
                    p.getHiddenReason() == null ? "" : p.getHiddenReason(),
                    p.getHiddenAt() == null ? "" : p.getHiddenAt().toString(),
                    p.getCreatedAt().toString()));
        }

        return new ReportResult(
                "Moderation Report",
                buildSubtitle(f),
                List.of("Post ID", "Author", "Email", "Preview", "Hidden Reason", "Hidden At", "Posted At"),
                rows);
    }

    private ReportResult buildAuditReport(ReportFilter f) {
        List<AuditLog> all = admin.allAuditLogs(MAX_AUDIT_ROWS);
        List<List<String>> rows = new ArrayList<>();

        for (AuditLog a : all) {
            if (!matchesDate(a.getCreatedAt().toString(), f))
                continue;

            if (f.search() != null && !f.search().isBlank()) {
                String q = f.search().toLowerCase();
                String hay = String.join(" ",
                        nvl(a.getActorName()),
                        nvl(a.getAction().name()),
                        nvl(a.getTargetType()),
                        nvl(a.getReason())).toLowerCase();
                if (!hay.contains(q))
                    continue;
            }

            rows.add(List.of(
                    a.getCreatedAt().toString(),
                    a.getActorName() == null ? "System" : a.getActorName(),
                    a.getActorRole() == null ? "" : a.getActorRole(),
                    a.getAction().name(),
                    a.getTargetType() == null ? "" : a.getTargetType(),
                    a.getTargetId() == null ? "" : a.getTargetId().toString(),
                    a.getReason() == null ? "" : a.getReason(),
                    a.getIpAddress() == null ? "" : a.getIpAddress(),
                    String.valueOf(a.isSuccess())));
        }

        return new ReportResult(
                "Audit Trail Report",
                buildSubtitle(f),
                List.of("Time", "Actor", "Role", "Action", "Target Type", "Target ID", "Reason", "IP", "Success"),
                rows);
    }

    private ReportResult buildDevotionsReport(ReportFilter f) {
        List<Devotion> all = admin.allDevotions();
        List<List<String>> rows = new ArrayList<>();

        for (Devotion d : all) {
            if (!matchesDate(d.getPublishedAt().toString(), f))
                continue;
            if (f.search() != null && !f.search().isBlank()) {
                String q = f.search().toLowerCase();
                if (!d.getTitle().toLowerCase().contains(q)
                        && !d.getScripture().toLowerCase().contains(q))
                    continue;
            }

            rows.add(List.of(
                    d.getId().toString(),
                    d.getTitle(),
                    d.getScripture(),
                    d.getAuthor() == null ? "—" : d.getAuthor().getName(),
                    String.valueOf(d.isPublished()),
                    String.valueOf(d.isFeatured()),
                    String.valueOf(d.isHidden()),
                    d.getPublishedAt().toString(),
                    shorten(d.getBody(), 100)));
        }

        return new ReportResult(
                "Devotions Report",
                buildSubtitle(f),
                List.of("ID", "Title", "Scripture", "Author", "Published", "Featured", "Hidden", "Published At",
                        "Body Preview"),
                rows);
    }

    private ReportResult buildTriviaReport(ReportFilter f) {
        List<TriviaQuestion> all = admin.allTrivia();
        List<List<String>> rows = new ArrayList<>();

        for (TriviaQuestion q : all) {
            if (!matchesDate(q.getCreatedAt().toString(), f))
                continue;
            if (f.search() != null && !f.search().isBlank()) {
                if (!q.getQuestion().toLowerCase().contains(f.search().toLowerCase()))
                    continue;
            }

            rows.add(List.of(
                    q.getId().toString(),
                    shorten(q.getQuestion(), 120),
                    q.getDifficulty(),
                    String.valueOf(q.isHidden()),
                    q.getCreatedAt().toString()));
        }

        return new ReportResult(
                "Trivia Report",
                buildSubtitle(f),
                List.of("ID", "Question", "Difficulty", "Hidden", "Created At"),
                rows);
    }

    /*
     * =========================================================
     * Helpers
     * =========================================================
     */

    private String buildSubtitle(ReportFilter f) {
        StringBuilder sb = new StringBuilder("Generated ");
        sb.append(Instant.now());
        if (f.from() != null)
            sb.append(" | From: ").append(f.from());
        if (f.to() != null)
            sb.append(" | To: ").append(f.to());
        if (f.search() != null && !f.search().isBlank())
            sb.append(" | Search: ").append(f.search());
        return sb.toString();
    }

    private static String shorten(String s, int max) {
        if (s == null)
            return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    /** Checks whether a row's createdAt (ISO string) falls in the filter range. */
    private static boolean matchesDate(String createdAtIso, ReportFilter f) {
        if (f.from() == null && f.to() == null)
            return true;
        if (createdAtIso == null)
            return true;
        try {
            Instant ts = Instant.parse(createdAtIso);
            if (f.from() != null && ts.isBefore(f.from()))
                return false;
            if (f.to() != null && ts.isAfter(f.to()))
                return false;
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}