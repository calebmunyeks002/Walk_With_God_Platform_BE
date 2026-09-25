package org.walkwithgod.call;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CallSessionRepository extends JpaRepository<CallSession, UUID> {

    /** All calls where the user is caller or callee. */
    @Query("""
                select c from CallSession c
                where c.callerId = :userId or c.calleeId = :userId
                order by c.createdAt desc
            """)
    Page<CallSession> findMyCalls(@Param("userId") UUID userId, Pageable pageable);

    /** Missed calls where the user is the callee. */
    long countByCalleeIdAndStatus(UUID calleeId, CallStatus status);
}