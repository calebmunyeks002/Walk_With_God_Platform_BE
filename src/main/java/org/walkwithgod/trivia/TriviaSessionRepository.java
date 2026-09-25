package org.walkwithgod.trivia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TriviaSessionRepository extends
        JpaRepository<TriviaSession, UUID>,
        JpaSpecificationExecutor<TriviaSession> {

    Optional<TriviaSession> findByUserIdAndQuizDateAndDifficulty(
            UUID userId, LocalDate quizDate, String difficulty);

    List<TriviaSession> findByUserIdAndQuizDate(UUID userId, LocalDate quizDate);

    long countByQuizDate(LocalDate quizDate);

    long countByQuizDateAndCompletedTrue(LocalDate quizDate);

    long countByQuizDateAndCompletedTrueAndPassedTrue(LocalDate quizDate);

    @Query("""
                select avg(s.score * 1.0 / nullif(s.total, 0))
                from TriviaSession s
                where s.quizDate = :date and s.completed = true
            """)
    Double avgScoreRatioForDate(@Param("date") LocalDate date);
}