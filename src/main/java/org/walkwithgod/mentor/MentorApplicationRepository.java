package org.walkwithgod.mentor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MentorApplicationRepository extends JpaRepository<MentorApplication, UUID> {

    /** Used by AdminService.stats() — count pending queue. */
    long countByStatus(String status);

    /** Used by MentorController.pending() — the review queue. */
    List<MentorApplication> findByStatusOrderByCreatedAtDesc(String status);

    /** Used by the admin mentor-applications page (all statuses). */
    List<MentorApplication> findAllByOrderByCreatedAtDesc();
}