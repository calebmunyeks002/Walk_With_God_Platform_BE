package org.walkwithgod.call;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.walkwithgod.user.AppUser;
import org.walkwithgod.user.UserRepository;

import java.util.*;

@RestController
@RequestMapping("/api/calls")
public class CallHistoryController {

    private final CallSessionRepository calls;
    private final UserRepository users;

    public CallHistoryController(CallSessionRepository calls, UserRepository users) {
        this.calls = calls;
        this.users = users;
    }

    public record CallView(
            String id,
            String callerId,
            String callerName,
            String calleeId,
            String calleeName,
            String direction, // "OUTGOING" | "INCOMING" relative to the requester
            String status,
            String mediaType,
            Integer durationSeconds,
            String startedAt,
            String answeredAt,
            String endedAt,
            String endReason) {
    }

    public record PageResponse<T>(
            List<T> content, long totalElements, int totalPages,
            int number, int size, boolean first, boolean last) {
    }

    @GetMapping
    public PageResponse<CallView> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        UUID uid = UUID.fromString(jwt.getSubject());
        Page<CallSession> result = calls.findMyCalls(uid, PageRequest.of(page, size));

        var content = result.getContent().stream().map(c -> {
            AppUser caller = users.findById(c.getCallerId()).orElse(null);
            AppUser callee = users.findById(c.getCalleeId()).orElse(null);
            return new CallView(
                    c.getId().toString(),
                    c.getCallerId().toString(),
                    caller == null ? "Unknown" : caller.getName(),
                    c.getCalleeId().toString(),
                    callee == null ? "Unknown" : callee.getName(),
                    c.getCallerId().equals(uid) ? "OUTGOING" : "INCOMING",
                    c.getStatus().name(),
                    c.getMediaType().name(),
                    c.getDurationSeconds(),
                    c.getStartedAt() == null ? null : c.getStartedAt().toString(),
                    c.getAnsweredAt() == null ? null : c.getAnsweredAt().toString(),
                    c.getEndedAt() == null ? null : c.getEndedAt().toString(),
                    c.getEndReason());
        }).toList();

        return new PageResponse<>(
                content,
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize(),
                result.isFirst(),
                result.isLast());
    }

    /** Number of missed calls — for a badge. */
    @GetMapping("/missed-count")
    public Map<String, Long> missedCount(@AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        long count = calls.countByCalleeIdAndStatus(uid, CallStatus.MISSED);
        return Map.of("count", count);
    }
}