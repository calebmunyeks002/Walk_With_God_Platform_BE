package org.walkwithgod.call;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.walkwithgod.audit.AuditAction;
import org.walkwithgod.audit.AuditService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class CallSignalingController {

    private final SimpMessagingTemplate broker;
    private final CallSessionRepository calls;
    private final AuditService audit;

    public CallSignalingController(
            SimpMessagingTemplate broker,
            CallSessionRepository calls,
            AuditService audit) {
        this.broker = broker;
        this.calls = calls;
        this.audit = audit;
    }

    /*
     * =========================================================
     * Handlers
     * =========================================================
     */

    @MessageMapping("/call/request")
    public void request(@Payload Map<String, Object> payload) {
        String fromUserId = (String) payload.get("fromUserId");
        String fromName = (String) payload.get("fromName");
        String calleeId = (String) payload.get("calleeId");
        String mediaType = (String) payload.getOrDefault("mediaType", "VIDEO");

        if (fromUserId == null || calleeId == null)
            return;

        CallSession c = new CallSession();
        c.setCallerId(UUID.fromString(fromUserId));
        c.setCalleeId(UUID.fromString(calleeId));
        c.setMediaType(CallMediaType.valueOf(mediaType));
        c.setStatus(CallStatus.RINGING);
        c.setStartedAt(Instant.now());
        calls.save(c);

        Map<String, Object> incoming = new HashMap<>();
        incoming.put("type", "CALL_INCOMING");
        incoming.put("callId", c.getId().toString());
        incoming.put("fromUserId", fromUserId);
        incoming.put("fromName", fromName == null ? "Unknown" : fromName);
        incoming.put("mediaType", mediaType);

        broker.convertAndSend("/topic/users/" + calleeId + "/calls", (Object) incoming);
    }

    @MessageMapping("/call/cancel")
    public void cancel(@Payload Map<String, Object> payload) {
        String callId = (String) payload.get("callId");
        String fromUserId = (String) payload.get("fromUserId");
        if (callId == null)
            return;

        CallSession c = calls.findById(UUID.fromString(callId)).orElse(null);
        if (c == null || c.getStatus() != CallStatus.RINGING)
            return;

        c.setStatus(CallStatus.MISSED);
        c.setEndedAt(Instant.now());
        c.setEndedBy(fromUserId == null ? null : UUID.fromString(fromUserId));
        c.setEndReason("CANCELLED");
        calls.save(c);

        notify(c.getCalleeId(), "CALL_CANCELLED", callId);
    }

    @MessageMapping("/call/accept")
    public void accept(@Payload Map<String, Object> payload) {
        String callId = (String) payload.get("callId");
        if (callId == null)
            return;

        CallSession c = calls.findById(UUID.fromString(callId)).orElse(null);
        if (c == null || c.getStatus() != CallStatus.RINGING)
            return;

        c.setStatus(CallStatus.ANSWERED);
        c.setAnsweredAt(Instant.now());
        calls.save(c);

        notify(c.getCallerId(), "CALL_ACCEPTED", callId);
        notify(c.getCalleeId(), "CALL_ACCEPTED", callId);
    }

    @MessageMapping("/call/decline")
    public void decline(@Payload Map<String, Object> payload) {
        String callId = (String) payload.get("callId");
        String fromUserId = (String) payload.get("fromUserId");
        if (callId == null)
            return;

        CallSession c = calls.findById(UUID.fromString(callId)).orElse(null);
        if (c == null || c.getStatus() != CallStatus.RINGING)
            return;

        c.setStatus(CallStatus.DECLINED);
        c.setEndedAt(Instant.now());
        c.setEndedBy(fromUserId == null ? null : UUID.fromString(fromUserId));
        c.setEndReason("DECLINED");
        calls.save(c);

        notify(c.getCallerId(), "CALL_DECLINED", callId);
    }

    @MessageMapping("/call/end")
    public void end(@Payload Map<String, Object> payload) {
        String callId = (String) payload.get("callId");
        String fromUserId = (String) payload.get("fromUserId");
        String reason = (String) payload.getOrDefault("reason", "HANGUP");
        if (callId == null)
            return;

        CallSession c = calls.findById(UUID.fromString(callId)).orElse(null);
        if (c == null)
            return;

        if (c.getStatus() == CallStatus.ANSWERED) {
            c.setEndedAt(Instant.now());
            if (c.getAnsweredAt() != null) {
                long secs = ChronoUnit.SECONDS.between(c.getAnsweredAt(), c.getEndedAt());
                c.setDurationSeconds((int) secs);
            }
            c.setStatus(CallStatus.ENDED);
        } else if (c.getStatus() == CallStatus.RINGING) {
            c.setStatus(CallStatus.MISSED);
            c.setEndedAt(Instant.now());
        }

        c.setEndedBy(fromUserId == null ? null : UUID.fromString(fromUserId));
        c.setEndReason(reason);
        calls.save(c);

        audit.record(AuditAction.USER_UPDATED, "CALL", c.getId(),
                "Call " + c.getStatus() + " duration=" +
                        (c.getDurationSeconds() == null ? 0 : c.getDurationSeconds()) + "s");

        notify(c.getCallerId(), "CALL_ENDED", callId);
        notify(c.getCalleeId(), "CALL_ENDED", callId);
    }

    /** Relay SDP offer/answer to the other peer. */
    @MessageMapping("/call/sdp")
    public void sdp(@Payload Map<String, Object> payload) {
        String toUserId = (String) payload.get("toUserId");
        if (toUserId == null)
            return;

        Map<String, Object> out = new HashMap<>(payload);
        out.put("type", "CALL_SDP");

        broker.convertAndSend("/topic/users/" + toUserId + "/calls", (Object) out);
    }

    /** Relay ICE candidate to the other peer. */
    @MessageMapping("/call/ice")
    public void ice(@Payload Map<String, Object> payload) {
        String toUserId = (String) payload.get("toUserId");
        if (toUserId == null)
            return;

        Map<String, Object> out = new HashMap<>(payload);
        out.put("type", "CALL_ICE");

        broker.convertAndSend("/topic/users/" + toUserId + "/calls", (Object) out);
    }

    /*
     * =========================================================
     * Helpers
     * =========================================================
     */

    private void notify(UUID userId, String type, String callId) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", type);
        msg.put("callId", callId);

        broker.convertAndSend("/topic/users/" + userId + "/calls", (Object) msg);
    }
}