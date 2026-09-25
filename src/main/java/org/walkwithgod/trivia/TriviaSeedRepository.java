package org.walkwithgod.trivia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TriviaSeedRepository extends JpaRepository<TriviaSeed, UUID> {

    List<TriviaSeed> findByActiveTrueAndDifficulty(String difficulty);

    long countByActiveTrueAndDifficulty(String difficulty);
}