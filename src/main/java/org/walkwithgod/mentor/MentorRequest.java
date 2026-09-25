package org.walkwithgod.mentor;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;
import org.walkwithgod.user.AppUser;

import java.time.Instant;

@Entity
@Table(name = "mentor_requests", indexes = {
        @Index(name = "idx_mr_member", columnList = "member_id"),
        @Index(name = "idx_mr_mentor", columnList = "mentor_id"),
        @Index(name = "idx_mr_mentor_status", columnList = "mentor_id, status")
})
public class MentorRequest extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private AppUser member;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "mentor_id")
    private MentorProfile mentor;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(length = 2000)
    private String message;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "response_note", length = 1000)
    private String responseNote;

    @Column(name = "completed_at")
    private Instant completedAt;

    // --- getters / setters ---

    public AppUser getMember() {
        return member;
    }

    public void setMember(AppUser v) {
        member = v;
    }

    public MentorProfile getMentor() {
        return mentor;
    }

    public void setMentor(MentorProfile v) {
        mentor = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String v) {
        message = v;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(Instant v) {
        respondedAt = v;
    }

    public String getResponseNote() {
        return responseNote;
    }

    public void setResponseNote(String v) {
        responseNote = v;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant v) {
        completedAt = v;
    }
}