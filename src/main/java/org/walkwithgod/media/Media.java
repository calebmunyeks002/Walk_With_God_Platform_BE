package org.walkwithgod.media;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "media", indexes = {
        @Index(name = "idx_media_uploader", columnList = "uploader_id"),
        @Index(name = "idx_media_post", columnList = "post_id"),
        @Index(name = "idx_media_status", columnList = "status"),
        @Index(name = "idx_media_checksum", columnList = "checksum_sha256")
})
public class Media extends BaseEntity {

    @Column(name = "uploader_id", nullable = false)
    private UUID uploaderId;

    @Column(name = "post_id")
    private UUID postId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModerationStatus status = ModerationStatus.APPROVED;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    private Integer width;
    private Integer height;

    @Column(name = "duration_seconds")
    private Double durationSeconds;

    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    @Column(name = "flagged_reason", length = 500)
    private String flaggedReason;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    // ---- getters / setters ----

    public UUID getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(UUID v) {
        uploaderId = v;
    }

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID v) {
        postId = v;
    }

    public MediaType getType() {
        return type;
    }

    public void setType(MediaType v) {
        type = v;
    }

    public ModerationStatus getStatus() {
        return status;
    }

    public void setStatus(ModerationStatus v) {
        status = v;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String v) {
        storageKey = v;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String v) {
        originalFilename = v;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String v) {
        contentType = v;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long v) {
        sizeBytes = v;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer v) {
        width = v;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer v) {
        height = v;
    }

    public Double getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Double v) {
        durationSeconds = v;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public void setChecksumSha256(String v) {
        checksumSha256 = v;
    }

    public String getFlaggedReason() {
        return flaggedReason;
    }

    public void setFlaggedReason(String v) {
        flaggedReason = v;
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