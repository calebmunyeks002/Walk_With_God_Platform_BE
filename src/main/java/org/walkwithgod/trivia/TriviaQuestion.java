package org.walkwithgod.trivia;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trivia_questions", indexes = {
        @Index(name = "idx_trivia_difficulty", columnList = "difficulty"),
        @Index(name = "idx_trivia_hidden", columnList = "hidden")
})
public class TriviaQuestion extends BaseEntity {

    @Column(nullable = false, length = 1000)
    private String question;

    @Column(name = "options_json", nullable = false, length = 3000)
    private String optionsJson;

    @Column(name = "answer_index", nullable = false)
    private int answerIndex;

    @Column(length = 1000)
    private String explanation;

    @Column(nullable = false, length = 20)
    private String difficulty = "EASY";

    // --- moderation ---

    @Column(nullable = false)
    private boolean hidden = false;

    @Column(name = "hidden_reason", length = 500)
    private String hiddenReason;

    @Column(name = "hidden_at")
    private Instant hiddenAt;

    @Column(name = "hidden_by")
    private UUID hiddenBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // --- getters / setters ---

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String v) {
        question = v;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public void setOptionsJson(String v) {
        optionsJson = v;
    }

    public int getAnswerIndex() {
        return answerIndex;
    }

    public void setAnswerIndex(int v) {
        answerIndex = v;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String v) {
        explanation = v;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String v) {
        difficulty = v;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean v) {
        hidden = v;
    }

    public String getHiddenReason() {
        return hiddenReason;
    }

    public void setHiddenReason(String v) {
        hiddenReason = v;
    }

    public Instant getHiddenAt() {
        return hiddenAt;
    }

    public void setHiddenAt(Instant v) {
        hiddenAt = v;
    }

    public UUID getHiddenBy() {
        return hiddenBy;
    }

    public void setHiddenBy(UUID v) {
        hiddenBy = v;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant v) {
        updatedAt = v;
    }
}