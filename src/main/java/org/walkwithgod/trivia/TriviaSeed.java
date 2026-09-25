package org.walkwithgod.trivia;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;

@Entity
@Table(name = "trivia_seeds", indexes = {
        @Index(name = "idx_seeds_type_diff", columnList = "type,difficulty,active")
})
public class TriviaSeed extends BaseEntity {

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, length = 10)
    private String difficulty;

    @Column(nullable = false, length = 1000)
    private String prompt;

    @Column(name = "correct_answer", nullable = false, length = 500)
    private String correctAnswer;

    @Column(name = "wrong_answers", nullable = false, length = 1000)
    private String wrongAnswers; // comma-separated

    @Column(length = 1000)
    private String explanation;

    @Column(name = "source_ref", length = 200)
    private String sourceRef;

    @Column(nullable = false)
    private boolean active = true;

    public String getType() {
        return type;
    }

    public void setType(String v) {
        type = v;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String v) {
        difficulty = v;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String v) {
        prompt = v;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String v) {
        correctAnswer = v;
    }

    public String getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(String v) {
        wrongAnswers = v;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String v) {
        explanation = v;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public void setSourceRef(String v) {
        sourceRef = v;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean v) {
        active = v;
    }
}