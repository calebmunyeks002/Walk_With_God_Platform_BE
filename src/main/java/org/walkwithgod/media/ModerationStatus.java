package org.walkwithgod.media;

public enum ModerationStatus {
    PENDING, // uploaded, waiting for review (only used if we ever go pending-first)
    APPROVED, // published without issues
    FLAGGED, // published, but queued for admin review (duplicate, etc.)
    REJECTED, // admin rejected — file hidden, kept for audit
    DELETED // soft-deleted, file removed from disk
}