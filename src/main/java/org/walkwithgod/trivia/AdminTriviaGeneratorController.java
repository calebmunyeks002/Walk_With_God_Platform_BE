package org.walkwithgod.trivia;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/trivia")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTriviaGeneratorController {

    private final TriviaGeneratorService generator;
    private final TriviaQuestionRepository questions;
    private final TriviaSeedRepository seeds;

    public AdminTriviaGeneratorController(
            TriviaGeneratorService generator,
            TriviaQuestionRepository questions,
            TriviaSeedRepository seeds) {
        this.generator = generator;
        this.questions = questions;
        this.seeds = seeds;
    }

    /** Pool sizes across the whole DB — used by the admin analytics page. */
    @GetMapping("/pool-stats")
    public Map<String, Long> poolStats() {
        return Map.of(
                "easy", questions.countByHiddenFalseAndDifficulty("EASY"),
                "medium", questions.countByHiddenFalseAndDifficulty("MEDIUM"),
                "hard", questions.countByHiddenFalseAndDifficulty("HARD"),
                "seedsEasy", seeds.countByActiveTrueAndDifficulty("EASY"),
                "seedsMedium", seeds.countByActiveTrueAndDifficulty("MEDIUM"),
                "seedsHard", seeds.countByActiveTrueAndDifficulty("HARD"));
    }

    /** Manually run the generator now (admin button). */
    @PostMapping("/generate-now")
    public Map<String, Integer> generateNow() {
        int created = generator.generateAll();
        return Map.of("created", created);
    }
}