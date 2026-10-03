package org.walkwithgod.mentor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MentorRequestRepository extends JpaRepository<MentorRequest, UUID> {

    /*
     * =========================================================
     * Existing queries used by MentorController
     * =========================================================
     */

    List<MentorRequest> findByMentorIdAndStatusOrderByCreatedAtDesc(
            UUID mentorId, String status);

    List<MentorRequest> findByMentorIdOrderByCreatedAtDesc(UUID mentorId);

    List<MentorRequest> findByMentorIdAndStatusInOrderByCreatedAtDesc(
            UUID mentorId, List<String> statuses);

    long countByMentorIdAndStatus(UUID mentorId, String status);

    /*
     * =========================================================
     * Batch 4.5 — mentor exclusivity
     * =========================================================
     */

    /** Active requests for a member — PENDING or ACCEPTED. */
    @Query("""
                SELECT r FROM MentorRequest r
                WHERE r.member.id = :memberId
                  AND r.status IN ('PENDING','ACCEPTED')
                ORDER BY r.createdAt DESC
            """)
    List<MentorRequest> findActiveForMember(@Param("memberId") UUID memberId);

    /** Accepted mentorships for a mentor's user id. */
    @Query("""
                SELECT r FROM MentorRequest r
                WHERE r.mentor.user.id = :mentorUserId
                  AND r.status = 'ACCEPTED'
                ORDER BY r.createdAt DESC
            """)
    List<MentorRequest> findAcceptedForMentorUser(@Param("mentorUserId") UUID mentorUserId);

    /** Other PENDING requests from the same member, excluding one. */
    @Query("""
                SELECT r FROM MentorRequest r
                WHERE r.member.id = :memberId
                  AND r.status = 'PENDING'
                  AND r.id <> :excludeId
            """)
    List<MentorRequest> findOtherPendingForMember(
            @Param("memberId") UUID memberId,
            @Param("excludeId") UUID excludeId);
}