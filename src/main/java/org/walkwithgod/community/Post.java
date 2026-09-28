package org.walkwithgod.community;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;
import org.walkwithgod.user.AppUser;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_posts_created", columnList = "createdAt"),
        @Index(name = "idx_posts_author", columnList = "author_id"),
        @Index(name = "idx_posts_hidden", columnList = "hidden"),
        @Index(name = "idx_posts_type", columnList = "type")
})
public class Post extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    private AppUser author;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(name = "scripture_reference", length = 160)
    private String scriptureReference;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostType type = PostType.NORMAL;

    @Column(name = "shared_from_id")
    private UUID sharedFromId;

    // --- moderation ---

    @Column(nullable = false)
    private boolean hidden = false;

    @Column(name = "hidden_reason", length = 500)
    private String hiddenReason;

    @Column(name = "hidden_at")
    private Instant hiddenAt;

    @Column(name = "community_id")
    private UUID communityId;
    public UUID getCommunityId() {
        return communityId; }
    public void setCommunityId(UUID v) {
        communityId = v; }

    @Column(name = "hidden_by")
    private UUID hiddenBy;

    @Column(name = "media_id")
    private UUID mediaId;

    public UUID getMediaId() {
        return mediaId;
    }

    public void setMediaId(UUID v) {
        mediaId = v;
    }

    // --- getters / setters ---

    public AppUser getAuthor() {
        return author;
    }

    public void setAuthor(AppUser v) {
        author = v;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String v) {
        content = v;
    }

    public String getScriptureReference() {
        return scriptureReference;
    }

    public void setScriptureReference(String v) {
        scriptureReference = v;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String v) {
        imageUrl = v;
    }

    public PostType getType() {
        return type;
    }

    public void setType(PostType v) {
        type = v;
    }

    public UUID getSharedFromId() {
        return sharedFromId;
    }

    public void setSharedFromId(UUID v) {
        sharedFromId = v;
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