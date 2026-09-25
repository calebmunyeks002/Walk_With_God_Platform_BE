package org.walkwithgod.admin;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.walkwithgod.audit.AuditAction;
import org.walkwithgod.audit.AuditService;
import org.walkwithgod.community.CommentRepository;
import org.walkwithgod.community.Post;
import org.walkwithgod.community.PostRepository;
import org.walkwithgod.devotion.Devotion;
import org.walkwithgod.devotion.DevotionRepository;
import org.walkwithgod.mentor.MentorApplicationRepository;
import org.walkwithgod.messaging.ConversationRepository;
import org.walkwithgod.moderation.Report;
import org.walkwithgod.moderation.ReportRepository;
import org.walkwithgod.moderation.ReportStatus;
import org.walkwithgod.moderation.ReportTargetType;
import org.walkwithgod.trivia.TriviaQuestion;
import org.walkwithgod.trivia.TriviaQuestionRepository;
import org.walkwithgod.user.AppUser;
import org.walkwithgod.user.Role;
import org.walkwithgod.user.UserRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.walkwithgod.admin.AdminDtos.*;

@Service
public class AdminService {

    private final UserRepository users;
    private final PostRepository posts;
    private final CommentRepository comments;
    private final DevotionRepository devotions;
    private final MentorApplicationRepository mentorApps;
    private final ConversationRepository conversations;
    private final ReportRepository reports;
    private final TriviaQuestionRepository trivia;
    private final ObjectMapper mapper;
    private final AuditService audit;
    private final org.walkwithgod.mentor.MentorRequestRepository mentorRequests;
    private final org.walkwithgod.audit.AuditLogRepository auditLogs;

    public AdminService(
            UserRepository users,
            PostRepository posts,
            CommentRepository comments,
            DevotionRepository devotions,
            MentorApplicationRepository mentorApps,
            ConversationRepository conversations,
            ReportRepository reports,
            TriviaQuestionRepository trivia,
            org.walkwithgod.mentor.MentorRequestRepository mentorRequests, // NEW
            org.walkwithgod.audit.AuditLogRepository auditLogs, // NEW
            ObjectMapper mapper,
            AuditService audit) {
        this.users = users;
        this.posts = posts;
        this.comments = comments;
        this.devotions = devotions;
        this.mentorApps = mentorApps;
        this.conversations = conversations;
        this.reports = reports;
        this.trivia = trivia;
        this.mentorRequests = mentorRequests;
        this.auditLogs = auditLogs;
        this.mapper = mapper;
        this.audit = audit;
    }
    /* ---------- Stats ---------- */

    public SystemStats stats() {
        return new SystemStats(
                users.count(),
                users.countByRole(Role.MEMBER),
                users.countByRole(Role.MENTOR),
                users.countByRole(Role.ADMIN),
                users.countBySuspendedTrue(),
                mentorApps.countByStatus("PENDING"),
                posts.count(),
                comments.count(),
                devotions.count(),
                conversations.count(),
                users.countByCreatedAtAfter(Instant.now().minus(1, ChronoUnit.DAYS)));
    }

    /* ---------- Users ---------- */

    public PageResponse<AdminUserView> listUsers(
            String search, Role role, Boolean enabled, int page, int size) {

        String s = (search == null || search.isBlank()) ? null : search.trim().toLowerCase();

        Specification<AppUser> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (s != null) {
                String pattern = "%" + s + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)));
            }
            if (role != null)
                predicates.add(cb.equal(root.get("role"), role));
            if (enabled != null)
                predicates.add(cb.equal(root.get("enabled"), enabled));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AppUser> result = users.findAll(spec, pr);

        return new PageResponse<>(
                result.getContent().stream().map(this::toUserView).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize(),
                result.isFirst(),
                result.isLast());
    }

    public AdminUserView suspend(String userId, String reason, Jwt actor) {
        AppUser u = users.findById(UUID.fromString(userId)).orElseThrow();
        u.setSuspended(true);
        u.setSuspensionReason(reason);
        u.setSuspendedAt(Instant.now());
        u.setSuspendedBy(UUID.fromString(actor.getSubject()));
        users.save(u);
        audit.record(AuditAction.USER_SUSPENDED, "USER", u.getId(), reason);
        return toUserView(u);
    }

    public AdminUserView reactivate(String userId, String reason) {
        AppUser u = users.findById(UUID.fromString(userId)).orElseThrow();
        u.setSuspended(false);
        u.setSuspensionReason(null);
        u.setSuspendedAt(null);
        u.setSuspendedBy(null);
        users.save(u);
        audit.record(AuditAction.USER_REACTIVATED, "USER", u.getId(), reason);
        return toUserView(u);
    }

    public AdminUserView changeRole(String userId, Role newRole, String reason) {
        AppUser u = users.findById(UUID.fromString(userId)).orElseThrow();
        Role oldRole = u.getRole();
        u.setRole(newRole);
        users.save(u);
        String fullReason = "Role changed from " + oldRole + " to " + newRole +
                (reason != null && !reason.isBlank() ? " — " + reason : "");
        audit.record(AuditAction.ROLE_CHANGED, "USER", u.getId(), fullReason);
        return toUserView(u);
    }

    private AdminUserView toUserView(AppUser u) {
        return new AdminUserView(
                u.getId().toString(),
                u.getName(),
                u.getEmail(),
                u.getRole(),
                u.isEnabled(),
                u.isSuspended(),
                u.getSuspensionReason(),
                u.getSuspendedAt() == null ? null : u.getSuspendedAt().toString(),
                u.getAvatarUrl(),
                u.getBio(),
                u.getCreatedAt().toString());
    }

    /* ---------- Posts moderation ---------- */

    public PageResponse<AdminPostView> listPosts(
            Boolean hidden, String search, int page, int size) {

        String s = (search == null || search.isBlank()) ? null : search.trim().toLowerCase();

        Specification<Post> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hidden != null)
                predicates.add(cb.equal(root.get("hidden"), hidden));
            if (s != null) {
                predicates.add(cb.like(cb.lower(root.get("content")), "%" + s + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> result = posts.findAll(spec, pr);

        return new PageResponse<>(
                result.getContent().stream().map(this::toPostView).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize(),
                result.isFirst(),
                result.isLast());
    }

    @Transactional
    public AdminPostView deletePost(String postId, String reason, Jwt actor) {
        Post p = posts.findById(UUID.fromString(postId)).orElseThrow();
        p.setHidden(true);
        p.setHiddenReason(reason);
        p.setHiddenAt(Instant.now());
        p.setHiddenBy(UUID.fromString(actor.getSubject()));
        posts.save(p);

        audit.record(AuditAction.POST_DELETED, "POST", p.getId(), reason);
        return toPostView(p);
    }

    @Transactional
    public AdminPostView restorePost(String postId, String reason) {
        Post p = posts.findById(UUID.fromString(postId)).orElseThrow();
        p.setHidden(false);
        p.setHiddenReason(null);
        p.setHiddenAt(null);
        p.setHiddenBy(null);
        posts.save(p);

        audit.record(AuditAction.POST_CREATED, "POST", p.getId(),
                "Restored" + (reason != null ? " — " + reason : ""));
        return toPostView(p);
    }

    private AdminPostView toPostView(Post p) {
        long reportCount = reports.countByTargetTypeAndTargetIdAndStatus(
                ReportTargetType.POST, p.getId(), ReportStatus.OPEN);

        return new AdminPostView(
                p.getId().toString(),
                p.getAuthor().getId().toString(),
                p.getAuthor().getName(),
                p.getAuthor().getEmail(),
                p.getContent(),
                p.getScriptureReference(),
                p.getImageUrl(),
                p.isHidden(),
                p.getHiddenReason(),
                p.getHiddenAt() == null ? null : p.getHiddenAt().toString(),
                reportCount,
                p.getCreatedAt().toString());
    }

    /* ---------- Reports ---------- */

    public PageResponse<ReportView> listReports(
            ReportStatus status, ReportTargetType targetType, int page, int size) {

        Specification<Report> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null)
                predicates.add(cb.equal(root.get("status"), status));
            if (targetType != null)
                predicates.add(cb.equal(root.get("targetType"), targetType));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Report> result = reports.findAll(spec, pr);

        return new PageResponse<>(
                result.getContent().stream().map(this::toReportView).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize(),
                result.isFirst(),
                result.isLast());
    }

    @Transactional
    public ReportView resolveReport(String reportId, ReportStatus status, String note, Jwt actor) {
        Report r = reports.findById(UUID.fromString(reportId)).orElseThrow();
        r.setStatus(status);
        r.setResolutionNote(note);
        r.setResolvedAt(Instant.now());
        r.setResolvedBy(UUID.fromString(actor.getSubject()));
        reports.save(r);

        audit.record(AuditAction.POST_REPORTED, "REPORT", r.getId(),
                "Status set to " + status + (note != null ? " — " + note : ""));
        return toReportView(r);
    }

    private ReportView toReportView(Report r) {
        return new ReportView(
                r.getId().toString(),
                r.getReporter().getId().toString(),
                r.getReporter().getName(),
                r.getTargetType(),
                r.getTargetId().toString(),
                r.getReason(),
                r.getDetails(),
                r.getStatus(),
                r.getResolvedBy() == null ? null : r.getResolvedBy().toString(),
                r.getResolvedAt() == null ? null : r.getResolvedAt().toString(),
                r.getResolutionNote(),
                r.getCreatedAt().toString());
    }

    /* ---------- Devotions ---------- */

    public PageResponse<AdminDevotionView> listDevotions(
            Boolean published, Boolean hidden, Boolean featured,
            String search, int page, int size) {

        String s = (search == null || search.isBlank()) ? null : search.trim().toLowerCase();

        Specification<Devotion> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (published != null)
                predicates.add(cb.equal(root.get("published"), published));
            if (hidden != null)
                predicates.add(cb.equal(root.get("hidden"), hidden));
            if (featured != null)
                predicates.add(cb.equal(root.get("featured"), featured));
            if (s != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), "%" + s + "%"),
                        cb.like(cb.lower(root.get("scripture")), "%" + s + "%")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<Devotion> result = devotions.findAll(spec, pr);

        return new PageResponse<>(
                result.getContent().stream().map(this::toDevotionView).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize(),
                result.isFirst(),
                result.isLast());
    }

    @Transactional
    public AdminDevotionView createDevotion(
            CreateDevotionRequest req, Jwt actor) {

        AppUser author = users.findById(UUID.fromString(actor.getSubject())).orElseThrow();

        Devotion d = new Devotion();
        d.setTitle(req.title());
        d.setScripture(req.scripture());
        d.setBody(req.body());
        d.setAuthor(author);
        d.setPublished(req.published() != null ? req.published() : true);
        d.setFeatured(req.featured() != null ? req.featured() : false);
        d.setPublishedAt(Instant.now());
        d.setUpdatedAt(Instant.now());
        devotions.save(d);

        audit.record(AuditAction.DEVOTION_PUBLISHED, "DEVOTION", d.getId(),
                "Created: " + d.getTitle());
        return toDevotionView(d);
    }

    @Transactional
    public AdminDevotionView updateDevotion(
            String id, UpdateDevotionRequest req) {

        Devotion d = devotions.findById(UUID.fromString(id)).orElseThrow();

        if (req.title() != null)
            d.setTitle(req.title());
        if (req.scripture() != null)
            d.setScripture(req.scripture());
        if (req.body() != null)
            d.setBody(req.body());
        if (req.published() != null) {
            boolean wasPublished = d.isPublished();
            d.setPublished(req.published());
            if (!wasPublished && req.published()) {
                d.setPublishedAt(Instant.now());
            }
        }
        if (req.featured() != null)
            d.setFeatured(req.featured());
        d.setUpdatedAt(Instant.now());

        devotions.save(d);

        audit.record(AuditAction.DEVOTION_PUBLISHED, "DEVOTION", d.getId(),
                "Updated: " + d.getTitle());
        return toDevotionView(d);
    }

    @Transactional
    public void deleteDevotion(String id, String reason, Jwt actor) {
        Devotion d = devotions.findById(UUID.fromString(id)).orElseThrow();
        d.setHidden(true);
        d.setHiddenReason(reason);
        d.setHiddenAt(Instant.now());
        d.setHiddenBy(UUID.fromString(actor.getSubject()));
        devotions.save(d);

        audit.record(AuditAction.DEVOTION_DELETED, "DEVOTION", d.getId(),
                reason != null ? reason : "Deleted");
    }

    private AdminDevotionView toDevotionView(Devotion d) {
        return new AdminDevotionView(
                d.getId().toString(),
                d.getTitle(),
                d.getScripture(),
                d.getBody(),
                d.getAuthor().getId().toString(),
                d.getAuthor().getName(),
                d.isPublished(),
                d.isFeatured(),
                d.isHidden(),
                d.getHiddenReason(),
                d.getPublishedAt() == null ? null : d.getPublishedAt().toString(),
                d.getUpdatedAt() == null ? null : d.getUpdatedAt().toString(),
                d.getCreatedAt().toString());
    }

    /*
     * =========================================================
     * Additional queries for report building
     * =========================================================
     */

    /** Raw mentor requests for reporting — newest first. */
    public List<org.walkwithgod.mentor.MentorRequest> allMentorRequests() {
        return mentorRequests.findAll(
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    /** Raw audit logs for reporting (up to a cap). */
    public List<org.walkwithgod.audit.AuditLog> allAuditLogs(int max) {
        var pageable = PageRequest.of(0, max,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return auditLogs.findAll(pageable).getContent();
    }

    /** Raw devotions for reporting — newest first. */
    public List<Devotion> allDevotions() {
        return devotions.findAll(
                Sort.by(Sort.Direction.DESC, "publishedAt"));
    }

    /** Raw trivia questions for reporting — newest first. */
    public List<TriviaQuestion> allTrivia() {
        return trivia.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /** All posts (with author pre-fetched) for reporting. */
    public List<Post> allPosts() {
        return posts.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /* ---------- Trivia ---------- */

    public PageResponse<AdminTriviaView> listTrivia(
            String difficulty, Boolean hidden, String search,
            int page, int size) {

        String s = (search == null || search.isBlank()) ? null : search.trim().toLowerCase();

        Specification<TriviaQuestion> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (difficulty != null && !difficulty.isBlank()) {
                predicates.add(cb.equal(root.get("difficulty"), difficulty.toUpperCase()));
            }
            if (hidden != null) {
                predicates.add(cb.equal(root.get("hidden"), hidden));
            }
            if (s != null) {
                predicates.add(cb.like(cb.lower(root.get("question")), "%" + s + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TriviaQuestion> result = trivia.findAll(spec, pr);

        return new PageResponse<>(
                result.getContent().stream().map(this::toTriviaView).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize(),
                result.isFirst(),
                result.isLast());
    }

    @Transactional
    public AdminTriviaView createTrivia(CreateTriviaRequest req) {
        validateTrivia(req.question(), req.options(), req.answerIndex(), req.difficulty());

        TriviaQuestion q = new TriviaQuestion();
        q.setQuestion(req.question().trim());
        q.setOptionsJson(writeOptions(req.options()));
        q.setAnswerIndex(req.answerIndex());
        q.setExplanation(req.explanation());
        q.setDifficulty(req.difficulty().toUpperCase());
        q.setUpdatedAt(Instant.now());
        trivia.save(q);

        audit.record(AuditAction.TRIVIA_CREATED, "TRIVIA", q.getId(),
                "Created: " + truncate(q.getQuestion(), 80));
        return toTriviaView(q);
    }

    @Transactional
    public AdminTriviaView updateTrivia(String id, UpdateTriviaRequest req) {
        TriviaQuestion q = trivia.findById(UUID.fromString(id)).orElseThrow();

        if (req.question() != null)
            q.setQuestion(req.question().trim());
        if (req.options() != null) {
            if (req.options().size() < 2) {
                throw new IllegalArgumentException("At least 2 options required");
            }
            q.setOptionsJson(writeOptions(req.options()));
        }
        if (req.answerIndex() != null) {
            List<String> opts = readOptions(q.getOptionsJson());
            if (req.answerIndex() < 0 || req.answerIndex() >= opts.size()) {
                throw new IllegalArgumentException("answerIndex out of range");
            }
            q.setAnswerIndex(req.answerIndex());
        }
        if (req.explanation() != null)
            q.setExplanation(req.explanation());
        if (req.difficulty() != null)
            q.setDifficulty(req.difficulty().toUpperCase());
        q.setUpdatedAt(Instant.now());

        trivia.save(q);

        audit.record(AuditAction.TRIVIA_CREATED, "TRIVIA", q.getId(),
                "Updated: " + truncate(q.getQuestion(), 80));
        return toTriviaView(q);
    }

    @Transactional
    public void deleteTrivia(String id, String reason, Jwt actor) {
        TriviaQuestion q = trivia.findById(UUID.fromString(id)).orElseThrow();
        q.setHidden(true);
        q.setHiddenReason(reason);
        q.setHiddenAt(Instant.now());
        q.setHiddenBy(UUID.fromString(actor.getSubject()));
        trivia.save(q);

        audit.record(AuditAction.TRIVIA_DELETED, "TRIVIA", q.getId(),
                reason != null ? reason : "Deleted");
    }

    private void validateTrivia(String question, List<String> options,
            Integer answerIndex, String difficulty) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question is required");
        }
        if (options == null || options.size() < 2) {
            throw new IllegalArgumentException("At least 2 options required");
        }
        if (answerIndex == null || answerIndex < 0 || answerIndex >= options.size()) {
            throw new IllegalArgumentException("answerIndex out of range");
        }
        if (difficulty == null || difficulty.isBlank()) {
            throw new IllegalArgumentException("Difficulty is required");
        }
        String d = difficulty.toUpperCase();
        if (!d.equals("EASY") && !d.equals("MEDIUM") && !d.equals("HARD")) {
            throw new IllegalArgumentException("Difficulty must be EASY, MEDIUM, or HARD");
        }
    }

    private String writeOptions(List<String> options) {
        try {
            return mapper.writeValueAsString(options);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to serialize options");
        }
    }

    private List<String> readOptions(String json) {
        try {
            return mapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private static String truncate(String s, int max) {
        if (s == null)
            return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    private AdminTriviaView toTriviaView(TriviaQuestion q) {
        return new AdminTriviaView(
                q.getId().toString(),
                q.getQuestion(),
                readOptions(q.getOptionsJson()),
                q.getAnswerIndex(),
                q.getExplanation(),
                q.getDifficulty(),
                q.isHidden(),
                q.getHiddenReason(),
                q.getHiddenAt() == null ? null : q.getHiddenAt().toString(),
                q.getUpdatedAt() == null ? null : q.getUpdatedAt().toString(),
                q.getCreatedAt().toString());
    }
}