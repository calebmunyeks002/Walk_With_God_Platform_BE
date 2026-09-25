package org.walkwithgod.messaging;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;
import org.walkwithgod.user.AppUser;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_messages_conversation_created", columnList = "conversation_id,createdAt")
})
public class Message extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Conversation conversation;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private AppUser sender;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(nullable = false)
    private boolean read = false;

    // --- getters / setters ---

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation v) {
        conversation = v;
    }

    public AppUser getSender() {
        return sender;
    }

    public void setSender(AppUser v) {
        sender = v;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String v) {
        content = v;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean v) {
        read = v;
    }
}