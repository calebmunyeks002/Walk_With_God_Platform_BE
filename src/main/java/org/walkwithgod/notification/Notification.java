package org.walkwithgod.notification;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_recipient", columnList = "recipient_id, created_at"),
        @Index(name = "idx_notifications_unread", columnList = "recipient_id, read"),
        @Index(name = "idx_notifications_type", columnList = "type")
})
public class Notification extends BaseEntity {

    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    @Column(name = "sender_id")
    private UUID senderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String body;

    @Column(length = 500)
    private String link;

    @Column(nullable = false)
    private boolean read = false;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "email_sent", nullable = false)
    private boolean emailSent = false;

    @Column(length = 2000)
    private String metadata;

    // --- getters / setters ---

    public UUID getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(UUID v) {
        recipientId = v;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID v) {
        senderId = v;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType v) {
        type = v;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String v) {
        title = v;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String v) {
        body = v;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String v) {
        link = v;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean v) {
        read = v;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant v) {
        readAt = v;
    }

    public boolean isEmailSent() {
        return emailSent;
    }

    public void setEmailSent(boolean v) {
        emailSent = v;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String v) {
        metadata = v;
    }
}