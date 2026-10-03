package org.walkwithgod.messaging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("""
                SELECT DISTINCT c FROM Conversation c
                JOIN c.participants p
                WHERE p.id = :userId
                ORDER BY c.createdAt DESC
            """)
    List<Conversation> findByParticipantsIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

    /** Find a 1:1 conversation between two users, regardless of order. */
    @Query("""
                SELECT c FROM Conversation c
                JOIN c.participants p
                WHERE p.id IN (:a, :b)
                GROUP BY c
                HAVING COUNT(DISTINCT p.id) = 2
            """)
    Optional<Conversation> findBetweenUsers(@Param("a") UUID a, @Param("b") UUID b);
}