package org.walkwithgod.bible;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BibleHighlightRepository extends JpaRepository<BibleHighlight, UUID> {

    Optional<BibleHighlight> findByUserIdAndVersionAndBookAndChapterAndVerse(
            UUID userId, String version, String book, int chapter, int verse);

    List<BibleHighlight> findByUserIdAndVersionAndBookAndChapterOrderByVerseAsc(
            UUID userId, String version, String book, int chapter);

    List<BibleHighlight> findByUserIdOrderByCreatedAtDesc(UUID userId);
}