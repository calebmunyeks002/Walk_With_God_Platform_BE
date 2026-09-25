package org.walkwithgod.messaging;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.walkwithgod.user.AppUser;
import org.walkwithgod.user.UserRepository;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final UserRepository users;
    private final SimpMessagingTemplate broker;

    public ConversationController(
            ConversationRepository conversations,
            MessageRepository messages,
            UserRepository users,
            SimpMessagingTemplate broker) {
        this.conversations = conversations;
        this.messages = messages;
        this.users = users;
        this.broker = broker;
    }

    public record ConversationView(
            String id,
            List<String> participantIds,
            String title,
            String lastMessage,
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

    @GetMapping
    public List<ConversationView> list(@AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        return conversations.findByParticipantsIdOrderByCreatedAtDesc(uid).stream()
                .map(c -> toView(c, uid))
                .toList();
    }

    @PostMapping
    public ConversationView start(
            @RequestBody StartRequest r,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        UUID other = UUID.fromString(r.participantId());

        // Reuse if a 1:1 conversation already exists
        Optional<Conversation> existing = conversations
                .findByParticipantsIdOrderByCreatedAtDesc(uid).stream()
                .filter(c -> c.getParticipants().size() == 2)
                .filter(c -> c.getParticipants().stream().anyMatch(p -> p.getId().equals(other)))
                .findFirst();

        if (existing.isPresent()) {
            return toView(existing.get(), uid);
        }

        AppUser me = users.findById(uid).orElseThrow();
        AppUser them = users.findById(other).orElseThrow();

        Conversation c = new Conversation();
        c.setTitle(me.getName() + " & " + them.getName());
        c.getParticipants().add(me);
        c.getParticipants().add(them);
        conversations.save(c);

        return toView(c, uid);
    }

    @GetMapping("/{id}/messages")
    public List<MessageView> messages(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        Conversation c = conversations.findById(id).orElseThrow();

        // Authorization — only participants can read
        if (c.getParticipants().stream().noneMatch(p -> p.getId().equals(uid))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Not a participant");
        }

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

        // Broadcast to every participant via their private topic
        for (AppUser p : c.getParticipants()) {
            broker.convertAndSend(
                    "/topic/users/" + p.getId() + "/messages",
                    view);
        }

        return view;
    }

    private ConversationView toView(Conversation c, UUID uid) {
        List<Message> msgs = messages.findByConversationIdOrderByCreatedAtAsc(c.getId());
        String last = msgs.isEmpty() ? null : msgs.get(msgs.size() - 1).getContent();

        return new ConversationView(
                c.getId().toString(),
                c.getParticipants().stream().map(p -> p.getId().toString()).toList(),
                c.getTitle(),
                last,
                c.getCreatedAt().toString());
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