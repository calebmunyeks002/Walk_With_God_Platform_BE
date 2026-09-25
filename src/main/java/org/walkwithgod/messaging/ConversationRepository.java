package org.walkwithgod.messaging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    /**
     * Find all conversations a user participates in.
     * Spring Data can't auto-derive this through the @ManyToMany join table,
     * so we use an explicit JPQL query.
     */
    @Query("""
                select distinct c from Conversation c
                join c.participants p
                where p.id = :userId
                order by c.createdAt desc
            """)
    List<Conversation> findByParticipantsIdOrderByCreatedAtDesc(@Param("userId") UUID userId);
}