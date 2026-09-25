package org.walkwithgod.trivia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
public class TriviaGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(TriviaGeneratorService.class);

    private final TriviaSeedRepository seeds;
    private final TriviaQuestionRepository questions;
    private final ObjectMapper mapper;

    public TriviaGeneratorService(
            TriviaSeedRepository seeds,
            TriviaQuestionRepository questions,
            ObjectMapper mapper) {
        this.seeds = seeds;
        this.questions = questions;
        this.mapper = mapper;
    }

    /**
     * Generates one question per active seed, shuffled options.
     * Skips any question whose content hash already exists in trivia_questions.
     */
    @Transactional
    public int generateAll() {
        int generated = 0;
        for (String difficulty : List.of("EASY", "MEDIUM", "HARD")) {
            generated += generateForDifficulty(difficulty);
        }
        log.info("Trivia generator produced {} new questions", generated);
        return generated;
    }

    @Transactional
    public int generateForDifficulty(String difficulty) {
        List<TriviaSeed> pool = seeds.findByActiveTrueAndDifficulty(difficulty);
        if (pool.isEmpty())
            return 0;

        Set<String> existingHashes = new HashSet<>();
        questions.findByHiddenFalse().forEach(q -> existingHashes.add(hash(q.getQuestion())));

        int generated = 0;
        for (TriviaSeed seed : pool) {
            String qText = seed.getPrompt().trim();
            String qHash = hash(qText);
            if (existingHashes.contains(qHash))
                continue;

            List<String> wrong = Arrays.stream(seed.getWrongAnswers().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();
            if (wrong.isEmpty())
                continue;

            // Assemble options with the correct answer
            List<String> options = new ArrayList<>(wrong);
            options.add(seed.getCorrectAnswer());

            // Shuffle options and track where the correct answer lands
            long seedValue = Math.abs(qHash.hashCode());
            Collections.shuffle(options, new Random(seedValue));
            int answerIndex = options.indexOf(seed.getCorrectAnswer());

            TriviaQuestion q = new TriviaQuestion();
            q.setQuestion(qText);
            q.setOptionsJson(writeOptions(options));
            q.setAnswerIndex(answerIndex);
            q.setExplanation(seed.getExplanation());
            q.setDifficulty(difficulty);
            questions.save(q);

            existingHashes.add(qHash);
            generated++;
        }
        return generated;
    }

    /*
     * =========================================================
     * Helpers
     * =========================================================
     */

    private String writeOptions(List<String> options) {
        try {
            return mapper.writeValueAsString(options);
        } catch (Exception e) {
            throw new IllegalStateException("JSON write failed", e);
        }
    }

    private static String hash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(s.toLowerCase().trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : out)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(s.hashCode());
        }
    }
}