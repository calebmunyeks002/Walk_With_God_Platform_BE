package org.walkwithgod.mentor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MentorProfileRepository extends JpaRepository<MentorProfile, UUID> {

    /** Public directory — verified mentors sorted by rating. */
    List<MentorProfile> findByVerifiedTrueOrderByRatingDesc();

    /** Used by the approval workflow — avoid duplicate profiles. */
    Optional<MentorProfile> findByUserId(UUID userId);
}