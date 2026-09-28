package org.walkwithgod.user;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "user_follows", uniqueConstraints = @UniqueConstraint(name = "uk_follow_pair", columnNames = {
        "follower_id", "followed_id" }))
public class UserFollow extends BaseEntity {

    @Column(name = "follower_id", nullable = false)
    private UUID followerId;

    @Column(name = "followed_id", nullable = false)
    private UUID followedId;

    public UUID getFollowerId() {
        return followerId;
    }

    public void setFollowerId(UUID v) {
        followerId = v;
    }

    public UUID getFollowedId() {
        return followedId;
    }

    public void setFollowedId(UUID v) {
        followedId = v;
    }
}