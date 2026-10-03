package org.walkwithgod.messaging;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.walkwithgod.user.AppUser;
import org.walkwithgod.user.UserRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class ConversationService {

    private final ConversationRepository conversations;
    private final UserRepository users;

    public ConversationService(ConversationRepository c, UserRepository u) {
        conversations = c;
        users = u;
    }

    /**
     * Idempotent — returns an existing conversation between the two users
     * or creates a fresh one.
     */
    @Transactional
    public Conversation getOrCreate(UUID userA, UUID userB) {
        Optional<Conversation> existing = conversations.findBetweenUsers(userA, userB);
        if (existing.isPresent())
            return existing.get();

        AppUser a = users.findById(userA)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userA));
        AppUser b = users.findById(userB)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userB));

        Conversation c = new Conversation();
        c.setTitle(a.getName() + " & " + b.getName());
        c.getParticipants().add(a);
        c.getParticipants().add(b);
        return conversations.save(c);
    }
}