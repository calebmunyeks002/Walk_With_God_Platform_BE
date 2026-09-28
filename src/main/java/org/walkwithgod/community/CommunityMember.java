package org.walkwithgod.community;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "community_members", uniqueConstraints = @UniqueConstraint(name = "uk_cm_pair", columnNames = {
        "community_id", "user_id" }))
public class CommunityMember extends BaseEntity {

    @Column(name = "community_id", nullable = false)
    private UUID communityId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunityRole role = CommunityRole.MEMBER;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt = Instant.now();

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

    public CommunityRole getRole() {
        return role;
    }

    public void setRole(CommunityRole v) {
        role = v;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant v) {
        joinedAt = v;
    }
}