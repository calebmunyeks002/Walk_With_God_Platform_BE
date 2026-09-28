package org.walkwithgod.community;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "communities", indexes = {
        @Index(name = "idx_community_slug", columnList = "slug", unique = true),
        @Index(name = "idx_community_creator", columnList = "creator_id"),
        @Index(name = "idx_community_hidden", columnList = "hidden"),
        @Index(name = "idx_community_visibility", columnList = "visibility")
})
public class Community extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 140, unique = true)
    private String slug;

    @Column(length = 2000)
    private String description;

    @Column(name = "cover_image_url", length = 1000)
    private String coverImageUrl;

    @Column(name = "icon_emoji", length = 20)
    private String iconEmoji = "🏛";

    @Column(name = "creator_id", nullable = false)
    private UUID creatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunityVisibility visibility = CommunityVisibility.PUBLIC;

    @Column(name = "member_count", nullable = false)
    private int memberCount = 0;

    @Column(name = "post_count", nullable = false)
    private int postCount = 0;

    @Column(nullable = false)
    private boolean hidden = false;

    @Column(name = "hidden_reason", length = 500)
    private String hiddenReason;

    @Column(name = "hidden_at")
    private Instant hiddenAt;

    @Column(name = "hidden_by")
    private UUID hiddenBy;

    // ---- getters / setters ----

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String v) {
        slug = v;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String v) {
        description = v;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String v) {
        coverImageUrl = v;
    }

    public String getIconEmoji() {
        return iconEmoji;
    }

    public void setIconEmoji(String v) {
        iconEmoji = v;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(UUID v) {
        creatorId = v;
    }

    public CommunityVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(CommunityVisibility v) {
        visibility = v;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int v) {
        memberCount = v;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int v) {
        postCount = v;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean v) {
        hidden = v;
    }

    public String getHiddenReason() {
        return hiddenReason;
    }

    public void setHiddenReason(String v) {
        hiddenReason = v;
    }

    public Instant getHiddenAt() {
        return hiddenAt;
    }

    public void setHiddenAt(Instant v) {
        hiddenAt = v;
    }

    public UUID getHiddenBy() {
        return hiddenBy;
    }

    public void setHiddenBy(UUID v) {
        hiddenBy = v;
    }
}