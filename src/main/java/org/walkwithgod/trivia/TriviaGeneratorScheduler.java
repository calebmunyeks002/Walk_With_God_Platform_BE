package org.walkwithgod.trivia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TriviaGeneratorScheduler {

    private static final Logger log = LoggerFactory.getLogger(TriviaGeneratorScheduler.class);

    private final TriviaGeneratorService generator;
    private final TriviaQuestionRepository questions;

    public TriviaGeneratorScheduler(
            TriviaGeneratorService generator,
            TriviaQuestionRepository questions) {
        this.generator = generator;
        this.questions = questions;
    }

    /** Runs every day at 3:00 AM server time. */
    @Scheduled(cron = "0 0 3 * * *", zone = "Africa/Nairobi")
    public void nightlyGenerate() {
        log.info("Trivia nightly generation starting…");
        long easy = questions.countByHiddenFalseAndDifficulty("EASY");
        long medium = questions.countByHiddenFalseAndDifficulty("MEDIUM");
        long hard = questions.countByHiddenFalseAndDifficulty("HARD");
        log.info("Pool sizes before: EASY={} MEDIUM={} HARD={}", easy, medium, hard);

        int created = generator.generateAll();

        log.info("Trivia nightly generation finished — {} questions added", created);
    }

    /** Ensure pools are seeded when the app starts (runs once after boot). */
    @jakarta.annotation.PostConstruct
    public void bootstrap() {
        try {
            Thread.sleep(5000); // small delay so Flyway finishes
            int created = generator.generateAll();
            if (created > 0) {
                log.info("Bootstrap seeded {} trivia questions from templates", created);
            }
        } catch (Exception e) {
            log.warn("Bootstrap trivia generation failed: {}", e.getMessage());
        }
    }
}