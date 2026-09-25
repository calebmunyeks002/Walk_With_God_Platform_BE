package org.walkwithgod.audit;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @Pointcut("@annotation(audited)")
    public void auditedMethods(Audited audited) {}

    @AfterReturning(pointcut = "auditedMethods(audited)", argNames = "jp,audited")
    public void record(JoinPoint jp, Audited audited) {
        String reason = extractReason(jp.getArgs());
        UUID targetId = extractTargetId(jp.getArgs());
        auditService.record(audited.action(), audited.targetType(), targetId, reason);
    }

    private String extractReason(Object[] args) {
        for (Object a : args) {
            if (a instanceof String s && s.length() <= 500 && s.toLowerCase().contains("reason")) {
                return s;
            }
        }
        return null;
    }

    /** Looks for a String argument that parses as a UUID — best-effort target ID. */
    private UUID extractTargetId(Object[] args) {
        for (Object a : args) {
            if (a instanceof UUID u) return u;
            if (a instanceof String s) {
                try { return UUID.fromString(s); } catch (Exception ignored) {}
            }
        }
        return null;
    }
}