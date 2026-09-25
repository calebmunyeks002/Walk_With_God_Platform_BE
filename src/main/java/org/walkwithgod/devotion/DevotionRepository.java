package org.walkwithgod.devotion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DevotionRepository extends
        JpaRepository<Devotion, UUID>,
        JpaSpecificationExecutor<Devotion> {

    /** Public feed — visible published devotions, newest first. */
    List<Devotion> findByPublishedTrueAndHiddenFalseOrderByPublishedAtDesc();

    /** Dashboard — the featured devotion (if any). */
    Optional<Devotion> findFirstByFeaturedTrueAndHiddenFalseAndPublishedTrue();

    /** Public reader by id, but only if visible. */
    Optional<Devotion> findByIdAndHiddenFalse(UUID id);
}