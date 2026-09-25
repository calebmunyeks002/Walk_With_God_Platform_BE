package org.walkwithgod.moderation;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;
import org.walkwithgod.user.AppUser;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reports", indexes = {
        @Index(name = "idx_reports_status", columnList = "status"),
        @Index(name = "idx_reports_target", columnList = "target_type, target_id")
})
public class Report extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "reporter_id")
    private AppUser reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(nullable = false, length = 80)
    private String reason;

    @Column(length = 2000)
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.OPEN;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolution_note", length = 1000)
    private String resolutionNote;

    // --- getters / setters ---

    public AppUser getReporter() {
        return reporter;
    }

    public void setReporter(AppUser v) {
        reporter = v;
    }

    public ReportTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(ReportTargetType v) {
        targetType = v;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID v) {
        targetId = v;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String v) {
        reason = v;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String v) {
        details = v;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus v) {
        status = v;
    }

    public UUID getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(UUID v) {
        resolvedBy = v;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant v) {
        resolvedAt = v;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String v) {
        resolutionNote = v;
    }
}