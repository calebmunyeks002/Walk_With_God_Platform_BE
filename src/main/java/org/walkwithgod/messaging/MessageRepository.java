package org.walkwithgod.messaging;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface MessageRepository extends JpaRepository<Message,UUID>{List<Message> findByConversationIdOrderByCreatedAtAsc(UUID id);}
