package org.walkwithgod.messaging;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.walkwithgod.user.AppUser;
import org.walkwithgod.user.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final UserRepository users;
    private final SimpMessagingTemplate broker;
    private final ConversationService conversationService;
    private final ConversationReadRepository reads;

    public ConversationController(
            ConversationRepository conversations,
            MessageRepository messages,
            UserRepository users,
            SimpMessagingTemplate broker,
            ConversationService conversationService,
            ConversationReadRepository reads) {
        this.conversations = conversations;
        this.messages = messages;
        this.users = users;
        this.broker = broker;
        this.conversationService = conversationService;
        this.reads = reads;
    }

    /*
     * =========================================================
     * DTOs
     * =========================================================
     */

    public record ConversationView(
            String id,
            List<String> participantIds,
            String title,
            String lastMessage,
            long unreadCount,
            String updatedAt) {
    }

    public record MessageView(
            String id,
            String conversationId,
            String senderId,
            String senderName,
            String content,
            String sentAt,
            boolean read) {
    }

    public record SendRequest(String content) {
    }

    public record StartRequest(String participantId) {
    }

    /*
     * =========================================================
     * Create / get by peer user
     * =========================================================
     */

    @PostMapping
    public ConversationView start(
            @RequestBody StartRequest r,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        UUID other = UUID.fromString(r.participantId());
        Conversation c = conversationService.getOrCreate(uid, other);
        return toView(c, uid);
    }

    @GetMapping("/with/{userId}")
    public ConversationView withUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        Conversation c = conversationService.getOrCreate(uid, userId);
        return toView(c, uid);
    }

    /*
     * =========================================================
     * List my conversations — sorted by most recent message
     * =========================================================
     */

    @GetMapping
    public List<ConversationView> list(@AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        return conversations.findByParticipantsIdOrderByCreatedAtDesc(uid).stream()
                .map(c -> toView(c, uid))
                // Sort by latest activity (last message time, or creation if empty)
                .sorted((a, b) -> b.updatedAt().compareTo(a.updatedAt()))
                .toList();
    }

    /*
     * =========================================================
     * Messages
     * =========================================================
     */

    @GetMapping("/{id}/messages")
    public List<MessageView> messages(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        Conversation c = conversations.findById(id).orElseThrow();

        if (c.getParticipants().stream().noneMatch(p -> p.getId().equals(uid))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Not a participant");
        }

        // Mark as read on fetch
        markRead(id, uid);

        return messages.findByConversationIdOrderByCreatedAtAsc(id).stream()
                .map(this::toMessageView)
                .toList();
    }

    @PostMapping("/{id}/messages")
    public MessageView send(
            @PathVariable UUID id,
            @RequestBody SendRequest r,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        Conversation c = conversations.findById(id).orElseThrow();

        if (c.getParticipants().stream().noneMatch(p -> p.getId().equals(uid))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Not a participant");
        }

        AppUser sender = users.findById(uid).orElseThrow();

        Message m = new Message();
        m.setConversation(c);
        m.setSender(sender);
        m.setContent(r.content());
        m.setRead(false);
        Message saved = messages.save(m);

        MessageView view = toMessageView(saved);

        // Bump sender's own last-read so their own messages don't count as unread
        markRead(id, uid);

        // Broadcast to every participant via their private topic
        for (AppUser p : c.getParticipants()) {
            broker.convertAndSend("/topic/users/" + p.getId() + "/messages", view);
        }

        return view;
    }

    /*
     * =========================================================
     * Mark conversation as read
     * =========================================================
     */

    @PostMapping("/{id}/read")
    public void markReadEndpoint(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        markRead(id, uid);
    }

    private void markRead(UUID conversationId, UUID userId) {
        ConversationRead r = reads.findByConversationIdAndUserId(conversationId, userId)
                .orElseGet(() -> {
                    ConversationRead n = new ConversationRead();
                    n.setConversationId(conversationId);
                    n.setUserId(userId);
                    return n;
                });
        r.setLastReadAt(Instant.now());
        reads.save(r);
    }

    /*
     * =========================================================
     * Mapping
     * =========================================================
     */

    private ConversationView toView(Conversation c, UUID uid) {
        List<Message> msgs = messages.findByConversationIdOrderByCreatedAtAsc(c.getId());

        String last = msgs.isEmpty() ? null : msgs.get(msgs.size() - 1).getContent();

        // Last activity time: latest message, else conversation creation
        Instant lastActivity = msgs.isEmpty()
                ? c.getCreatedAt()
                : msgs.get(msgs.size() - 1).getCreatedAt();

        // Unread count: messages not from me, after my last read
        Instant lastRead = reads.findByConversationIdAndUserId(c.getId(), uid)
                .map(ConversationRead::getLastReadAt)
                .orElse(Instant.EPOCH);

        long unread = messages.countByConversationIdAndSenderIdNotAndCreatedAtAfter(
                c.getId(), uid, lastRead);

        return new ConversationView(
                c.getId().toString(),
                c.getParticipants().stream().map(p -> p.getId().toString()).toList(),
                c.getTitle(),
                last,
                unread,
                lastActivity.toString());
    }

    private MessageView toMessageView(Message m) {
        return new MessageView(
                m.getId().toString(),
                m.getConversation().getId().toString(),
                m.getSender().getId().toString(),
                m.getSender().getName(),
                m.getContent(),
                m.getCreatedAt().toString(),
                m.isRead());
    }
}