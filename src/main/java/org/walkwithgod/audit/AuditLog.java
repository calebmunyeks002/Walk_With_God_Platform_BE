package org.walkwithgod.audit;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "actor_name", length = 120)
    private String actorName;

    @Column(name = "actor_role", length = 20)
    private String actorRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private AuditAction action;

    @Column(name = "target_type", length = 60)
    private String targetType;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(length = 500)
    private String reason;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 400)
    private String userAgent;

    @Column(columnDefinition = "text")
    private String metadata;
    
    @Column(nullable = false)
    private boolean success = true;

    // --- getters / setters ---

    public UUID getActorId() { return actorId; }
    public void setActorId(UUID v) { actorId = v; }

    public String getActorName() { return actorName; }
    public void setActorName(String v) { actorName = v; }

    public String getActorRole() { return actorRole; }
    public void setActorRole(String v) { actorRole = v; }

    public AuditAction getAction() { return action; }
    public void setAction(AuditAction v) { action = v; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String v) { targetType = v; }

    public UUID getTargetId() { return targetId; }
    public void setTargetId(UUID v) { targetId = v; }

    public String getReason() { return reason; }
    public void setReason(String v) { reason = v; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String v) { ipAddress = v; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String v) { userAgent = v; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String v) { metadata = v; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean v) { success = v; }
}