package org.walkwithgod.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface NotificationRepository extends
        JpaRepository<Notification, UUID>,
        JpaSpecificationExecutor<Notification> {

    /** User's own notifications. */
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    /** Unread count for the bell badge. */
    long countByRecipientIdAndReadFalse(UUID recipientId);

    /** Mark-all-read helper. */
    @Query("""
                update Notification n
                set n.read = true, n.readAt = CURRENT_TIMESTAMP
                where n.recipientId = :recipientId and n.read = false
            """)
    @org.springframework.data.jpa.repository.Modifying
    int markAllReadForRecipient(@Param("recipientId") UUID recipientId);
}