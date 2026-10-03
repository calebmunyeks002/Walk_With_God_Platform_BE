package org.walkwithgod.mentor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.walkwithgod.messaging.ConversationService;
import org.walkwithgod.notification.NotificationService;
import org.walkwithgod.notification.NotificationType;
import org.walkwithgod.user.AppUser;
import org.walkwithgod.user.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MentorRequestService {

    private final MentorRequestRepository requests;
    private final MentorProfileRepository mentors;
    private final UserRepository users;
    private final ConversationService conversations;
    private final NotificationService notifications;

    public MentorRequestService(
            MentorRequestRepository r,
            MentorProfileRepository m,
            UserRepository u,
            ConversationService c,
            NotificationService n) {
        requests = r;
        mentors = m;
        users = u;
        conversations = c;
        notifications = n;
    }

    /*
     * =========================================================
     * Send request
     * =========================================================
     */

    @Transactional
    public MentorRequest sendRequest(UUID memberId, UUID mentorId, String message) {
        // 1. Member must not already have an active request
        List<MentorRequest> active = requests.findActiveForMember(memberId);
        if (!active.isEmpty()) {
            MentorRequest existing = active.get(0);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You already have a " + existing.getStatus().toLowerCase()
                            + " mentorship request. End or cancel it before requesting a new mentor.");
        }

        // 2. Load member + mentor
        AppUser member = users.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        MentorProfile mentor = mentors.findById(mentorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mentor not found"));

        // 3. Create the request
        MentorRequest req = new MentorRequest();
        req.setMember(member);
        req.setMentor(mentor);
        req.setMessage(message);
        req.setStatus(MentorRequestStatus.PENDING); // String constant — works
        requests.save(req);

        // 4. Notify the mentor
        AppUser mentorUser = mentor.getUser();
        if (mentorUser != null) {
            notifications.create(
                    mentorUser.getId(),
                    member.getId(),
                    NotificationType.MENTOR_REQUEST,
                    "New mentorship request from " + member.getName(),
                    message == null || message.isBlank()
                            ? "They'd like you to mentor them."
                            : message,
                    "/mentor/requests",
                    true);
        }

        return req;
    }

    /*
     * =========================================================
     * Accept
     * =========================================================
     */

    @Transactional
    public MentorRequest accept(UUID requestId, UUID mentorUserId, String note) {
        MentorRequest req = requests.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (req.getMentor() == null || req.getMentor().getUser() == null
                || !req.getMentor().getUser().getId().equals(mentorUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (!MentorRequestStatus.PENDING.equals(req.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Request is no longer pending");
        }

        // Rule B1: auto-cancel the member's other PENDING requests
        List<MentorRequest> others = requests.findOtherPendingForMember(
                req.getMember().getId(), req.getId());
        for (MentorRequest other : others) {
            other.setStatus(MentorRequestStatus.DECLINED);
            other.setDeclinedAt(Instant.now());
            other.setRespondedAt(Instant.now());
            other.setEndReason("Member matched with another mentor");
            requests.save(other);

            AppUser otherMentorUser = other.getMentor() != null ? other.getMentor().getUser() : null;
            if (otherMentorUser != null) {
                notifications.create(
                        otherMentorUser.getId(),
                        null,
                        NotificationType.MENTOR_DECLINED,
                        "Request auto-cancelled",
                        other.getMember().getName() + " has been matched with another mentor",
                        "/mentor/requests",
                        false);
            }
        }

        // Accept this one
        req.setStatus(MentorRequestStatus.ACCEPTED);
        req.setAcceptedAt(Instant.now());
        req.setRespondedAt(Instant.now());
        req.setResponseNote(note);
        requests.save(req);

        // Auto-create conversation
        AppUser mentorUser = req.getMentor().getUser();
        AppUser member = req.getMember();
        if (mentorUser != null && member != null) {
            conversations.getOrCreate(mentorUser.getId(), member.getId());
        }

        // Notify the member
        if (mentorUser != null && member != null) {
            notifications.create(
                    member.getId(),
                    mentorUser.getId(),
                    NotificationType.MENTOR_ACCEPTED,
                    mentorUser.getName() + " accepted your mentorship request",
                    note != null && !note.isBlank()
                            ? note
                            : "You can now message them anytime in your inbox.",
                    "/inbox?user=" + mentorUser.getId(),
                    true);
        }

        return req;
    }

    /*
     * =========================================================
     * Decline
     * =========================================================
     */

    @Transactional
    public MentorRequest decline(UUID requestId, UUID mentorUserId, String reason) {
        MentorRequest req = requests.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (req.getMentor() == null || req.getMentor().getUser() == null
                || !req.getMentor().getUser().getId().equals(mentorUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (!MentorRequestStatus.PENDING.equals(req.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        req.setStatus(MentorRequestStatus.DECLINED);
        req.setDeclinedAt(Instant.now());
        req.setRespondedAt(Instant.now());
        req.setResponseNote(reason);
        requests.save(req);

        AppUser mentorUser = req.getMentor().getUser();
        if (mentorUser != null) {
            notifications.create(
                    req.getMember().getId(),
                    mentorUser.getId(),
                    NotificationType.MENTOR_DECLINED,
                    mentorUser.getName() + " couldn't take on new mentees right now",
                    reason != null && !reason.isBlank()
                            ? reason
                            : "You can try another mentor from the directory.",
                    "/mentors",
                    false);
        }

        return req;
    }

    /*
     * =========================================================
     * End an active mentorship
     * =========================================================
     */

    @Transactional
    public MentorRequest end(UUID requestId, UUID actorUserId, String reason) {
        MentorRequest req = requests.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        AppUser mentorUser = req.getMentor() != null ? req.getMentor().getUser() : null;
        AppUser member = req.getMember();

        UUID mentorUserId = mentorUser != null ? mentorUser.getId() : null;
        UUID memberId = member != null ? member.getId() : null;

        if (!actorUserId.equals(mentorUserId) && !actorUserId.equals(memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (!MentorRequestStatus.ACCEPTED.equals(req.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        req.setStatus(MentorRequestStatus.COMPLETED);
        req.setEndedAt(Instant.now());
        req.setCompletedAt(Instant.now());
        req.setEndReason(reason);
        requests.save(req);

        UUID other = actorUserId.equals(mentorUserId) ? memberId : mentorUserId;
        String actorName = users.findById(actorUserId).map(AppUser::getName).orElse("User");
        if (other != null) {
            notifications.create(
                    other,
                    actorUserId,
                    NotificationType.MENTOR_DECLINED,
                    "Mentorship ended",
                    actorName + " ended the mentorship",
                    "/mentors",
                    false);
        }

        return req;
    }

    /*
     * =========================================================
     * Query: my current active mentorship
     * =========================================================
     */

    public MentorRequest getActiveForMember(UUID memberId) {
        List<MentorRequest> active = requests.findActiveForMember(memberId);
        if (active.isEmpty())
            return null;
        return active.stream()
                .filter(r -> MentorRequestStatus.ACCEPTED.equals(r.getStatus()))
                .findFirst()
                .orElse(active.get(0));
    }
}