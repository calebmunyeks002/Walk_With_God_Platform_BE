package org.walkwithgod.mentor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MentorRequestRepository extends JpaRepository<MentorRequest, UUID> {

    /** Pending queue for a specific mentor, newest first. */
    List<MentorRequest> findByMentorIdAndStatusOrderByCreatedAtDesc(UUID mentorId, String status);

    /** All requests for a mentor (any status), newest first. */
    List<MentorRequest> findByMentorIdOrderByCreatedAtDesc(UUID mentorId);

    /** Accepted mentees for a mentor, newest first. */
    List<MentorRequest> findByMentorIdAndStatusInOrderByCreatedAtDesc(UUID mentorId, List<String> statuses);

    /** Count active mentees. */
    long countByMentorIdAndStatus(UUID mentorId, String status);

    /** Count completed requests. */
    long countByMentorIdAndStatusIn(UUID mentorId, List<String> statuses);

    /** Member's own outgoing requests. */
    List<MentorRequest> findByMemberIdOrderByCreatedAtDesc(UUID memberId);
}