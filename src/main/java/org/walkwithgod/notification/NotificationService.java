package org.walkwithgod.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.walkwithgod.user.AppUser;
import org.walkwithgod.user.UserRepository;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notifications;
    private final UserRepository users;
    private final EmailService email;
    private final SimpMessagingTemplate broker;

    public NotificationService(
            NotificationRepository notifications,
            UserRepository users,
            EmailService email,
            SimpMessagingTemplate broker) {
        this.notifications = notifications;
        this.users = users;
        this.email = email;
        this.broker = broker;
    }

    /*
     * =========================================================
     * Create + push + email
     * =========================================================
     */

    @Transactional
    public Notification create(
            UUID recipientId,
            UUID senderId,
            NotificationType type,
            String title,
            String body,
            String link,
            boolean sendEmail) {
        Notification n = new Notification();
        n.setRecipientId(recipientId);
        n.setSenderId(senderId);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        n.setLink(link);
        n.setRead(false);
        notifications.save(n);

        // Push over WebSocket — bell badge updates live
        Map<String, Object> payload = Map.of(
                "type", "NOTIFICATION",
                "notification", Map.of(
                        "id", n.getId().toString(),
                        "type", n.getType().name(),
                        "title", n.getTitle(),
                        "body", n.getBody() == null ? "" : n.getBody(),
                        "link", n.getLink() == null ? "" : n.getLink(),
                        "read", false,
                        "createdAt", n.getCreatedAt().toString()));
        broker.convertAndSend("/topic/users/" + recipientId + "/notifications", (Object) payload);

        // Optional email
        if (sendEmail) {
            users.findById(recipientId).ifPresent(u -> {
                email.sendNotificationEmail(u.getEmail(), title, body == null ? "" : body, link);
                n.setEmailSent(true);
                notifications.save(n);
            });
        }

        return n;
    }

    /*
     * =========================================================
     * Mark read
     * =========================================================
     */

    @Transactional
    public void markRead(UUID notificationId, UUID recipientId) {
        notifications.findById(notificationId).ifPresent(n -> {
            if (!n.getRecipientId().equals(recipientId))
                return;
            n.setRead(true);
            n.setReadAt(Instant.now());
            notifications.save(n);
        });
    }

    @Transactional
    public int markAllRead(UUID recipientId) {
        return notifications.markAllReadForRecipient(recipientId);
    }

    /*
     * =========================================================
     * Queries
     * =========================================================
     */

    public Page<Notification> listForUser(UUID recipientId, Pageable pageable) {
        return notifications.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable);
    }

    public long unreadCount(UUID recipientId) {
        return notifications.countByRecipientIdAndReadFalse(recipientId);
    }

    /*
     * =========================================================
     * Convenience wrappers
     * =========================================================
     */

    /** Welcome email + in-app notification on registration. */
    @Transactional
    public void welcome(AppUser user) {
        // Fire email
        String firstName = user.getName().split(" ")[0];
        email.sendWelcomeEmail(user.getEmail(), firstName);

        // Also create a gentle in-app notification
        Notification n = new Notification();
        n.setRecipientId(user.getId());
        n.setType(NotificationType.WELCOME);
        n.setTitle("Welcome to WalkWithGod ✨");
        n.setBody("We're glad you're here, " + firstName
                + ". Start by reading the Bible, following devotions, or finding a mentor.");
        n.setLink("/dashboard");
        n.setRead(false);
        n.setEmailSent(true);
        notifications.save(n);

        broker.convertAndSend(
                "/topic/users/" + user.getId() + "/notifications",
                (Object) Map.of(
                        "type", "NOTIFICATION",
                        "notification", Map.of(
                                "id", n.getId().toString(),
                                "type", n.getType().name(),
                                "title", n.getTitle(),
                                "body", n.getBody(),
                                "link", n.getLink(),
                                "read", false,
                                "createdAt", n.getCreatedAt().toString())));
    }
}