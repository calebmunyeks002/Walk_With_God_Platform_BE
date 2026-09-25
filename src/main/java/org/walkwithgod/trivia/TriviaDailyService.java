package org.walkwithgod.trivia;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.walkwithgod.notification.NotificationService;
import org.walkwithgod.notification.NotificationType;
import org.walkwithgod.settings.PlatformSettingRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class TriviaDailyService {

    private static final int DEFAULT_QUESTIONS = 10;
    private static final double PASS_RATIO = 0.6;

    private final TriviaSessionRepository sessions;
    private final TriviaQuestionRepository questions;
    private final NotificationService notifications;
    private final PlatformSettingRepository settings;

    public TriviaDailyService(
            TriviaSessionRepository sessions,
            TriviaQuestionRepository questions,
            NotificationService notifications,
            PlatformSettingRepository settings) {
        this.sessions = sessions;
        this.questions = questions;
        this.notifications = notifications;
        this.settings = settings;
    }

    /** How many questions per session (from platform settings). */
    private int questionsPerSession() {
        return settings.findByKey("questionsPerSession")
                .map(s -> {
                    try {
                        return Integer.parseInt(s.getValue().trim());
                    } catch (Exception e) {
                        return DEFAULT_QUESTIONS;
                    }
                })
                .orElse(DEFAULT_QUESTIONS);
    }

    @Transactional
    public TriviaSession getOrCreateToday(UUID userId, String difficultyRaw) {
        String difficulty = normalizeDifficulty(difficultyRaw);
        LocalDate today = LocalDate.now();

        Optional<TriviaSession> existing = sessions.findByUserIdAndQuizDateAndDifficulty(userId, today, difficulty);
        if (existing.isPresent())
            return existing.get();

        int want = questionsPerSession();

        List<TriviaQuestion> randomPick = questions.randomByDifficulty(difficulty, want);
        final List<TriviaQuestion> picked = new ArrayList<>(randomPick);

        // Fallback: pull from any visible pool if the tier is thin
        if (picked.size() < want) {
            List<TriviaQuestion> extra = questions.findByHiddenFalse().stream()
                    .filter(q -> !picked.contains(q))
                    .limit(want - picked.size())
                    .toList();
            picked.addAll(extra);
        }

        String csv = picked.stream()
                .map(q -> q.getId().toString())
                .reduce((a, b) -> a + "," + b)
                .orElse("");

        TriviaSession s = new TriviaSession();
        s.setUserId(userId);
        s.setQuizDate(today);
        s.setDifficulty(difficulty);
        s.setQuestionIds(csv);
        s.setTotal(picked.size());
        sessions.save(s);
        return s;
    }

    public record AttemptAnswer(String questionId, int selectedIndex) {
    }

    public record AttemptResult(
            int score, int total, boolean passed, boolean newlyPassed) {
    }

    @Transactional
    public AttemptResult recordAttempt(
            UUID userId, String difficultyRaw, List<AttemptAnswer> answers) {
        String difficulty = normalizeDifficulty(difficultyRaw);
        TriviaSession s = getOrCreateToday(userId, difficulty);

        Set<UUID> ids = new HashSet<>();
        for (String sId : s.getQuestionIds().split(",")) {
            if (!sId.isBlank())
                ids.add(UUID.fromString(sId.trim()));
        }

        Map<UUID, TriviaQuestion> byId = new HashMap<>();
        questions.findAllById(ids).forEach(q -> byId.put(q.getId(), q));

        int score = 0;
        for (AttemptAnswer a : answers) {
            if (a.questionId() == null)
                continue;
            TriviaQuestion q;
            try {
                q = byId.get(UUID.fromString(a.questionId()));
            } catch (Exception e) {
                continue;
            }
            if (q != null && q.getAnswerIndex() == a.selectedIndex())
                score++;
        }

        int total = Math.max(s.getTotal(), answers.size());
        boolean passed = total > 0 && ((double) score / total) >= PASS_RATIO;
        boolean wasPassed = s.isPassed();

        s.setCompleted(true);
        s.setPassed(passed || wasPassed);
        s.setScore(Math.max(score, s.getScore()));
        s.setTotal(total);
        s.setCompletedAt(Instant.now());
        sessions.save(s);

        boolean newlyPassed = passed && !wasPassed;

        if (newlyPassed) {
            notifications.create(
                    userId, null,
                    NotificationType.ADMIN_BROADCAST,
                    "🎉 Trivia passed!",
                    "You scored " + score + "/" + total + " on " + difficulty
                            + " trivia. Well done!",
                    "/trivia", false);
        }

        return new AttemptResult(score, total, s.isPassed(), newlyPassed);
    }

    private static String normalizeDifficulty(String raw) {
        if (raw == null)
            return "EASY";
        String r = raw.trim().toUpperCase();
        if (!r.equals("EASY") && !r.equals("MEDIUM") && !r.equals("HARD"))
            return "EASY";
        return r;
    }
}