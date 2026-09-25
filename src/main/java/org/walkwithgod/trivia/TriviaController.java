package org.walkwithgod.trivia;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trivia")
public class TriviaController {

    private final TriviaQuestionRepository questions;
    private final TriviaSessionRepository sessions;
    private final TriviaDailyService daily;
    private final ObjectMapper mapper;

    public TriviaController(
            TriviaQuestionRepository questions,
            TriviaSessionRepository sessions,
            TriviaDailyService daily,
            ObjectMapper mapper) {
        this.questions = questions;
        this.sessions = sessions;
        this.daily = daily;
        this.mapper = mapper;
    }

    /*
     * =========================================================
     * DTOs
     * =========================================================
     */

    public record QuestionView(
            String id,
            String question,
            List<String> options,
            String difficulty) {
    }

    public record TodayQuizView(
            String difficulty,
            int total,
            boolean completed,
            boolean passed,
            int score,
            List<QuestionView> questions) {
    }

    public record AnswerDto(
            @NotNull String questionId,
            @NotNull Integer selectedIndex) {
    }

    public record SubmitRequest(
            @NotNull String difficulty,
            @NotNull List<AnswerDto> answers) {
    }

    public record SubmitResult(
            int score,
            int total,
            boolean passed,
            boolean newlyPassed) {
    }

    /*
     * =========================================================
     * GET /api/trivia/today?difficulty=EASY
     * Returns (and creates if needed) today's quiz.
     * Hidden questions are never returned. answers are stripped.
     * =========================================================
     */

    @GetMapping("/today")
    public TodayQuizView today(
            @RequestParam(defaultValue = "EASY") String difficulty,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        TriviaSession s = daily.getOrCreateToday(uid, difficulty);

        List<UUID> ids = parseIds(s.getQuestionIds());
        Map<UUID, TriviaQuestion> byId = new HashMap<>();
        questions.findAllById(ids).forEach(q -> byId.put(q.getId(), q));

        List<QuestionView> views = ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(q -> new QuestionView(
                        q.getId().toString(),
                        q.getQuestion(),
                        readOptions(q.getOptionsJson()),
                        q.getDifficulty()))
                .toList();

        return new TodayQuizView(
                s.getDifficulty(),
                s.getTotal(),
                s.isCompleted(),
                s.isPassed(),
                s.getScore(),
                views);
    }

    /*
     * =========================================================
     * POST /api/trivia/today/submit
     * Records the attempt.
     * =========================================================
     */

    @PostMapping("/today/submit")
    public SubmitResult submit(
            @Valid @RequestBody SubmitRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        List<TriviaDailyService.AttemptAnswer> answers = req.answers().stream()
                .map(a -> new TriviaDailyService.AttemptAnswer(a.questionId(), a.selectedIndex()))
                .collect(Collectors.toList());

        TriviaDailyService.AttemptResult r = daily.recordAttempt(uid, req.difficulty(), answers);
        return new SubmitResult(r.score(), r.total(), r.passed(), r.newlyPassed());
    }

    /*
     * =========================================================
     * Helpers
     * =========================================================
     */

    private List<UUID> parseIds(String csv) {
        if (csv == null || csv.isBlank())
            return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(UUID::fromString)
                .toList();
    }

    private List<String> readOptions(String json) {
        try {
            return mapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}