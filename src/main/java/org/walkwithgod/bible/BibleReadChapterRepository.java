package org.walkwithgod.bible;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BibleReadChapterRepository extends JpaRepository<BibleReadChapter, UUID> {

    Optional<BibleReadChapter> findByUserIdAndBookAndChapter(UUID userId, String book, int chapter);

    boolean existsByUserIdAndBookAndChapter(UUID userId, String book, int chapter);

    List<BibleReadChapter> findByUserIdOrderByReadAtDesc(UUID userId);

    long countByUserId(UUID userId);

    long countByUserIdAndBook(UUID userId, String book);
}