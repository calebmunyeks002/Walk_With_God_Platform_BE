package org.walkwithgod.devotion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DevotionRepository
        extends JpaRepository<Devotion, UUID>,
        JpaSpecificationExecutor<Devotion> { // ← ADD THIS

    List<Devotion> findByPublishedTrueAndHiddenFalseOrderByPublishedAtDesc();

    Optional<Devotion> findByIdAndHiddenFalse(UUID id);

    Optional<Devotion> findFirstByFeaturedTrueAndHiddenFalseAndPublishedTrue();

    // ---- Batch 2 additions for DevotionScheduler ----
    boolean existsByPublishedAtBetweenAndSystemGeneratedFalse(Instant from, Instant to);

    Optional<Devotion> findFirstBySystemGeneratedTrueOrderByPublishedAtDesc();
}