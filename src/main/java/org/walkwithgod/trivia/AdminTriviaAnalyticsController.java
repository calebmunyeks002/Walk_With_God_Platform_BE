package org.walkwithgod.trivia;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.walkwithgod.user.AppUser;
import org.walkwithgod.user.UserRepository;

import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/admin/trivia")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTriviaAnalyticsController {

    private final TriviaSessionRepository sessions;
    private final UserRepository users;
    private final TriviaDailyNotifier notifier;

    /** Single constructor — Spring injects all three. */
    public AdminTriviaAnalyticsController(
            TriviaSessionRepository sessions,
            UserRepository users,
            TriviaDailyNotifier notifier) {
        this.sessions = sessions;
        this.users = users;
        this.notifier = notifier;
    }

    /** Admin-only: trigger the midnight notification immediately (for testing). */
    @PostMapping("/notify-now")
    public Map<String, Long> notifyNow() {
        return Map.of("sent", notifier.runNow());
    }

    /*
     * =========================================================
     * Overview for a specific date
     * =========================================================
     */

    public record TriviaOverview(
            String date,
            long totalSessions,
            long completedSessions,
            long passedSessions,
            long failedSessions,
            double completionRate,
            double passRate,
            double avgScoreRatio,
            Map<String, Long> byDifficulty) {
    }

    @GetMapping("/overview")
    public TriviaOverview overview(
            @RequestParam(required = false) String date) {
        LocalDate d = date == null || date.isBlank()
                ? LocalDate.now()
                : LocalDate.parse(date);

        List<TriviaSession> all = sessions.findAll(
                (Specification<TriviaSession>) (root, q, cb) -> cb.equal(root.get("quizDate"), d));

        long total = all.size();
        long completed = all.stream().filter(TriviaSession::isCompleted).count();
        long passed = all.stream().filter(TriviaSession::isPassed).count();
        long failed = completed - passed;

        double completionRate = total == 0 ? 0 : (double) completed / total;
        double passRate = completed == 0 ? 0 : (double) passed / completed;

        Double avgRatio = sessions.avgScoreRatioForDate(d);
        double avgScore = avgRatio == null ? 0 : avgRatio;

        Map<String, Long> byDifficulty = new HashMap<>();
        all.forEach(s -> byDifficulty.merge(
                s.getDifficulty(), 1L, Long::sum));

        return new TriviaOverview(
                d.toString(),
                total, completed, passed, failed,
                completionRate, passRate, avgScore, byDifficulty);
    }

    /*
     * =========================================================
     * Detailed session list with filters
     * =========================================================
     */

    public record SessionRow(
            String id,
            String userId,
            String userName,
            String userEmail,
            String date,
            String difficulty,
            boolean completed,
            boolean passed,
            int score,
            int total,
            String completedAt) {
    }

    public record PageResponse<T>(
            List<T> content, long totalElements, int totalPages,
            int number, int size, boolean first, boolean last) {
    }

    @GetMapping("/sessions")
    public PageResponse<SessionRow> sessions(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Boolean passed,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        final LocalDate d = (date == null || date.isBlank())
                ? null
                : LocalDate.parse(date);
        final String diff = (difficulty == null || difficulty.isBlank() || "ALL".equalsIgnoreCase(difficulty))
                ? null
                : difficulty.toUpperCase();
        final UUID uid = (userId == null || userId.isBlank())
                ? null
                : UUID.fromString(userId);
        final String s = (search == null || search.isBlank())
                ? null
                : search.trim().toLowerCase();

        Specification<TriviaSession> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (d != null)
                predicates.add(cb.equal(root.get("quizDate"), d));
            if (diff != null)
                predicates.add(cb.equal(root.get("difficulty"), diff));
            if (completed != null)
                predicates.add(cb.equal(root.get("completed"), completed));
            if (passed != null)
                predicates.add(cb.equal(root.get("passed"), passed));
            if (uid != null)
                predicates.add(cb.equal(root.get("userId"), uid));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        var pr = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TriviaSession> result = sessions.findAll(spec, pr);

        List<SessionRow> rows = result.getContent().stream().map(x -> {
            AppUser u = users.findById(x.getUserId()).orElse(null);
            return new SessionRow(
                    x.getId().toString(),
                    x.getUserId().toString(),
                    u == null ? "Unknown" : u.getName(),
                    u == null ? "Unknown" : u.getEmail(),
                    x.getQuizDate().toString(),
                    x.getDifficulty(),
                    x.isCompleted(),
                    x.isPassed(),
                    x.getScore(),
                    x.getTotal(),
                    x.getCompletedAt() == null ? null : x.getCompletedAt().toString());
        }).toList();

        // Client-side search on user name/email
        if (s != null) {
            rows = rows.stream().filter(r -> r.userName().toLowerCase().contains(s)
                    || r.userEmail().toLowerCase().contains(s)).toList();
        }

        return new PageResponse<>(
                rows,
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize(),
                result.isFirst(),
                result.isLast());
    }
}