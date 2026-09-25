package org.walkwithgod.devotion;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;
import org.walkwithgod.user.AppUser;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "devotions", indexes = {
        @Index(name = "idx_devotions_published", columnList = "publishedAt"),
        @Index(name = "idx_devotions_featured", columnList = "featured"),
        @Index(name = "idx_devotions_hidden", columnList = "hidden")
})
public class Devotion extends BaseEntity {

    @Column(nullable = false, length = 240)
    private String title;

    @Column(nullable = false, length = 160)
    private String scripture;

    @Column(nullable = false, length = 15000)
    private String body;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    private AppUser author;

    @Column(nullable = false)
    private boolean published = true;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt = Instant.now();

    @Column(nullable = false)
    private boolean featured = false;

    // --- moderation / admin ---

    @Column(nullable = false)
    private boolean hidden = false;

    @Column(name = "hidden_reason", length = 500)
    private String hiddenReason;

    @Column(name = "hidden_at")
    private Instant hiddenAt;

    @Column(name = "hidden_by")
    private UUID hiddenBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // --- getters / setters ---

    public String getTitle() {
        return title;
    }

    public void setTitle(String v) {
        title = v;
    }

    public String getScripture() {
        return scripture;
    }

    public void setScripture(String v) {
        scripture = v;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String v) {
        body = v;
    }

    public AppUser getAuthor() {
        return author;
    }

    public void setAuthor(AppUser v) {
        author = v;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean v) {
        published = v;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant v) {
        publishedAt = v;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean v) {
        featured = v;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant v) {
        updatedAt = v;
    }
}