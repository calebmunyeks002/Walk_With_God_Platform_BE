package org.walkwithgod.community;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "community_join_requests", uniqueConstraints = @UniqueConstraint(name = "uk_cjr_open", columnNames = {
        "community_id", "user_id" }))
public class CommunityJoinRequest extends BaseEntity {

    @Column(name = "community_id", nullable = false)
    private UUID communityId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JoinRequestStatus status = JoinRequestStatus.PENDING;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    public UUID getCommunityId() {
        return communityId;
    }

    public void setCommunityId(UUID v) {
        communityId = v;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID v) {
        userId = v;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String v) {
        message = v;
    }

    public JoinRequestStatus getStatus() {
        return status;
    }

    public void setStatus(JoinRequestStatus v) {
        status = v;
    }

    public UUID getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(UUID v) {
        reviewedBy = v;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant v) {
        reviewedAt = v;
    }
}