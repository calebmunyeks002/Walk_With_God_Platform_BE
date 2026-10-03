package org.walkwithgod.mentor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.walkwithgod.user.AppUser;
import org.walkwithgod.user.UserRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mentor-requests")
public class MentorRequestController {

    private final MentorRequestRepository requests;
    private final MentorProfileRepository mentors;
    private final UserRepository users;
    private final MentorRequestService service;

    public MentorRequestController(
            MentorRequestRepository r,
            MentorProfileRepository m,
            UserRepository u,
            MentorRequestService s) {
        requests = r;
        mentors = m;
        users = u;
        service = s;
    }

    /*
     * =========================================================
     * DTOs
     * =========================================================
     */

    public record SendRequest(
            @NotNull UUID mentorId,
            @Size(max = 1000) String message) {
    }

    public record ActionRequest(@Size(max = 500) String note) {
    }

    public record MemberView(String id, String name, String email, String avatarUrl) {
    }

    public record MentorView(
            String id, String userId, String name, String email, String avatarUrl) {
    }

    public record RequestView(
            String id,
            String status,
            String message,
            MemberView member,
            MentorView mentor,
            String createdAt,
            String acceptedAt,
            String declinedAt,
            String endedAt,
            String endReason) {
    }

    public record MineResponse(
            RequestView activeMentorship,
            RequestView pendingRequest,
            boolean canRequestNew) {
    }

    /*
     * =========================================================
     * Send
     * =========================================================
     */

    @PostMapping
    public RequestView send(
            @Valid @RequestBody SendRequest r,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        MentorRequest req = service.sendRequest(uid, r.mentorId(), r.message());
        return toView(req);
    }

    /*
     * =========================================================
     * Member: what's my state?
     * =========================================================
     */

    @GetMapping("/mine")
    public MineResponse mine(@AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        MentorRequest active = service.getActiveForMember(uid);

        if (active == null) {
            return new MineResponse(null, null, true);
        }
        if (MentorRequestStatus.ACCEPTED.equals(active.getStatus())) {
            return new MineResponse(toView(active), null, false);
        }
        return new MineResponse(null, toView(active), false);
    }

    /*
     * =========================================================
     * Mentor: incoming requests
     * =========================================================
     */

    @GetMapping("/incoming")
    public List<RequestView> incoming(@AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());

        MentorProfile profile = mentors.findByUserId(uid).orElse(null);
        if (profile == null)
            return List.of();

        return requests
                .findByMentorIdAndStatusOrderByCreatedAtDesc(
                        profile.getId(), MentorRequestStatus.PENDING)
                .stream()
                .map(this::toView)
                .toList();
    }

    /*
     * =========================================================
     * Accept / decline / end
     * =========================================================
     */

    @PostMapping("/{id}/accept")
    public RequestView accept(
            @PathVariable UUID id,
            @RequestBody(required = false) ActionRequest r,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        MentorRequest req = service.accept(id, uid, r != null ? r.note() : null);
        return toView(req);
    }

    @PostMapping("/{id}/decline")
    public RequestView decline(
            @PathVariable UUID id,
            @Valid @RequestBody ActionRequest r,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        MentorRequest req = service.decline(id, uid, r.note());
        return toView(req);
    }

    @PostMapping("/{id}/end")
    public RequestView end(
            @PathVariable UUID id,
            @RequestBody(required = false) ActionRequest r,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        MentorRequest req = service.end(id, uid, r != null ? r.note() : null);
        return toView(req);
    }

    /*
     * =========================================================
     * Mapping
     * =========================================================
     */

    private RequestView toView(MentorRequest r) {
        AppUser memberUser = r.getMember();
        MentorProfile mentor = r.getMentor();
        AppUser mentorUser = mentor != null ? mentor.getUser() : null;

        return new RequestView(
                r.getId().toString(),
                r.getStatus(), // ← String, no .name()
                r.getMessage(),
                memberUser == null ? null
                        : new MemberView(
                                memberUser.getId().toString(),
                                memberUser.getName(),
                                memberUser.getEmail(),
                                memberUser.getAvatarUrl()),
                mentorUser == null ? null
                        : new MentorView(
                                mentor.getId().toString(),
                                mentorUser.getId().toString(),
                                mentorUser.getName(),
                                mentorUser.getEmail(),
                                mentorUser.getAvatarUrl()),
                r.getCreatedAt().toString(),
                r.getAcceptedAt() == null ? null : r.getAcceptedAt().toString(),
                r.getDeclinedAt() == null ? null : r.getDeclinedAt().toString(),
                r.getEndedAt() == null ? null : r.getEndedAt().toString(),
                r.getEndReason());
    }
}