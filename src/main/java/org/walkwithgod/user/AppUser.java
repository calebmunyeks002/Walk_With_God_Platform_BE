package org.walkwithgod.user;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email", unique = true),
    @Index(name = "idx_users_role", columnList = "role")
})
public class AppUser extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 190, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.MEMBER;

    @Column(length = 500)
    private String avatarUrl;

    @Column(length = 1000)
    private String bio;

    @Column(nullable = false)
    private boolean enabled = true;

    // --- suspension ---
    @Column(nullable = false)
    private boolean suspended = false;

    @Column(name = "suspension_reason", length = 500)
    private String suspensionReason;

    @Column(name = "suspended_at")
    private Instant suspendedAt;

    @Column(name = "suspended_by")
    private UUID suspendedBy;

    // --- getters / setters ---

    public String getName() { return name; }
    public void setName(String v) { name = v; }

    public String getEmail() { return email; }
    public void setEmail(String v) { email = v == null ? null : v.toLowerCase().trim(); }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String v) { passwordHash = v; }

    public Role getRole() { return role; }
    public void setRole(Role v) { role = v; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String v) { avatarUrl = v; }

    public String getBio() { return bio; }
    public void setBio(String v) { bio = v; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) { enabled = v; }

    public boolean isSuspended() { return suspended; }
    public void setSuspended(boolean v) { suspended = v; }

    public String getSuspensionReason() { return suspensionReason; }
    public void setSuspensionReason(String v) { suspensionReason = v; }

    public Instant getSuspendedAt() { return suspendedAt; }
    public void setSuspendedAt(Instant v) { suspendedAt = v; }

    public UUID getSuspendedBy() { return suspendedBy; }
    public void setSuspendedBy(UUID v) { suspendedBy = v; }
}