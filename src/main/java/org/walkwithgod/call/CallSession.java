package org.walkwithgod.call;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "call_sessions", indexes = {
        @Index(name = "idx_calls_caller", columnList = "caller_id, created_at"),
        @Index(name = "idx_calls_callee", columnList = "callee_id, created_at"),
        @Index(name = "idx_calls_status", columnList = "status")
})
public class CallSession extends BaseEntity {

    @Column(name = "caller_id", nullable = false)
    private UUID callerId;

    @Column(name = "callee_id", nullable = false)
    private UUID calleeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CallStatus status = CallStatus.RINGING;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 10)
    private CallMediaType mediaType = CallMediaType.VIDEO;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "answered_at")
    private Instant answeredAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "ended_by")
    private UUID endedBy;

    @Column(name = "end_reason", length = 40)
    private String endReason;

    // --- getters / setters ---

    public UUID getCallerId() {
        return callerId;
    }

    public void setCallerId(UUID v) {
        callerId = v;
    }

    public UUID getCalleeId() {
        return calleeId;
    }

    public void setCalleeId(UUID v) {
        calleeId = v;
    }

    public CallStatus getStatus() {
        return status;
    }

    public void setStatus(CallStatus v) {
        status = v;
    }

    public CallMediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(CallMediaType v) {
        mediaType = v;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant v) {
        startedAt = v;
    }

    public Instant getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(Instant v) {
        answeredAt = v;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant v) {
        endedAt = v;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer v) {
        durationSeconds = v;
    }

    public UUID getEndedBy() {
        return endedBy;
    }

    public void setEndedBy(UUID v) {
        endedBy = v;
    }

    public String getEndReason() {
        return endReason;
    }

    public void setEndReason(String v) {
        endReason = v;
    }
}