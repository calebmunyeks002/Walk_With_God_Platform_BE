package org.walkwithgod.trivia;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "trivia_sessions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_trivia_session_user_day_diff", columnNames = { "user_id", "quiz_date",
                "difficulty" })
})
public class TriviaSession extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "quiz_date", nullable = false)
    private LocalDate quizDate;

    @Column(nullable = false, length = 10)
    private String difficulty;

    /** Comma-separated UUIDs — 5 questions. */
    @Column(name = "question_ids", nullable = false, length = 2000)
    private String questionIds;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(nullable = false)
    private boolean passed = false;

    @Column(nullable = false)
    private int score = 0;

    @Column(nullable = false)
    private int total = 0;

    @Column(name = "completed_at")
    private Instant completedAt;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID v) {
        userId = v;
    }

    public LocalDate getQuizDate() {
        return quizDate;
    }

    public void setQuizDate(LocalDate v) {
        quizDate = v;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String v) {
        difficulty = v;
    }

    public String getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(String v) {
        questionIds = v;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean v) {
        completed = v;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean v) {
        passed = v;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int v) {
        score = v;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int v) {
        total = v;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant v) {
        completedAt = v;
    }
}