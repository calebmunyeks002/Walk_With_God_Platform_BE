package org.walkwithgod.trivia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface TriviaQuestionRepository extends
        JpaRepository<TriviaQuestion, UUID>,
        JpaSpecificationExecutor<TriviaQuestion> {

    /** Public quiz — visible questions only. */
    List<TriviaQuestion> findByHiddenFalse();

    /** Public quiz — visible questions, optional difficulty filter. */
    List<TriviaQuestion> findByHiddenFalseAndDifficulty(String difficulty);

    /** Count questions per difficulty. */
    long countByHiddenFalseAndDifficulty(String difficulty);

    /** Random sample — pulls a set number of visible questions of a difficulty. */
    @Query(value = """
                select * from trivia_questions
                where hidden = false and difficulty = :difficulty
                order by random()
                limit :limit
            """, nativeQuery = true)
    List<TriviaQuestion> randomByDifficulty(
            @Param("difficulty") String difficulty,
            @Param("limit") int limit);
}