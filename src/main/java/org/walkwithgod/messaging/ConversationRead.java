package org.walkwithgod.messaging;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation_reads", uniqueConstraints = @UniqueConstraint(name = "uk_cr_pair", columnNames = {
        "conversation_id", "user_id" }))
public class ConversationRead extends BaseEntity {

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "last_read_at", nullable = false)
    private Instant lastReadAt = Instant.now();

    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID v) {
        conversationId = v;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID v) {
        userId = v;
    }

    public Instant getLastReadAt() {
        return lastReadAt;
    }

    public void setLastReadAt(Instant v) {
        lastReadAt = v;
    }
}