package org.walkwithgod.mentor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.walkwithgod.audit.AuditAction;
import org.walkwithgod.audit.AuditService;
import org.walkwithgod.auth.AuthDtos;
import org.walkwithgod.notification.NotificationService;
import org.walkwithgod.notification.NotificationType;
import org.walkwithgod.user.AppUser;
import org.walkwithgod.user.Role;
import org.walkwithgod.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.walkwithgod.mentor.MentorDtos.*;

@RestController
@RequestMapping("/api")
public class MentorController {

        private final MentorProfileRepository mentors;
        private final MentorRequestRepository requests;
        private final MentorApplicationRepository applications;
        private final UserRepository users;
        private final AuditService audit;
        private final NotificationService notifications;
        private final MentorRequestService mentorRequestService;

        public MentorController(
                        MentorProfileRepository mentors,
                        MentorRequestRepository requests,
                        MentorApplicationRepository applications,
                        UserRepository users,
                        AuditService audit,
                        NotificationService notifications,
                        MentorRequestService mentorRequestService) {
                this.mentors = mentors;
                this.requests = requests;
                this.applications = applications;
                this.users = users;
                this.audit = audit;
                this.notifications = notifications;
                this.mentorRequestService = mentorRequestService;
        }

        /*
         * =========================================================
         * DTOs
         * =========================================================
         */

        public record MentorView(
                        String id,
                        AuthDtos.UserView user,
                        String denomination,
                        String church,
                        String[] specialties,
                        String bio,
                        int yearsExperience,
                        boolean verified,
                        Double rating,
                        boolean isMyMentor,
                        String mentorshipStatus) {
        }

        public record ApplicationDto(
                        @NotBlank String qualifications,
                        @Size(max = 300) String organization,
                        int yearsExperience,
                        @Size(max = 1000) String documentUrl) {
        }

        public record PendingApplicationView(
                        String id,
                        String applicantId,
                        String name,
                        String email,
                        String qualifications,
                        String organization,
                        int yearsExperience,
                        String documentUrl,
                        String status,
                        String createdAt) {
        }

        public record ReviewDto(@NotBlank String status, String reason) {
        }

        /*
         * =========================================================
         * Public — mentor directory (with exclusivity info)
         * =========================================================
         */

        @GetMapping("/mentors")
        public List<MentorView> list(@AuthenticationPrincipal Jwt jwt) {
                UUID uid = UUID.fromString(jwt.getSubject());

                MentorRequest active = mentorRequestService.getActiveForMember(uid);
                UUID myMentorUserId = null;
                String status = null;

                if (active != null
                                && active.getMentor() != null
                                && active.getMentor().getUser() != null) {
                        myMentorUserId = active.getMentor().getUser().getId();
                        status = active.getStatus();
                }

                final UUID finalMyMentorUserId = myMentorUserId;
                final String finalStatus = status;

                return mentors.findByVerifiedTrueOrderByRatingDesc().stream()
                                .map(m -> {
                                        boolean isMine = finalMyMentorUserId != null
                                                        && m.getUser() != null
                                                        && m.getUser().getId().equals(finalMyMentorUserId);

                                        String[] specialties = m.getSpecialties() == null
                                                        ? new String[0]
                                                        : Arrays.stream(m.getSpecialties().split(","))
                                                                        .map(String::trim)
                                                                        .filter(s -> !s.isEmpty())
                                                                        .toArray(String[]::new);

                                        return new MentorView(
                                                        m.getId().toString(),
                                                        toUserView(m.getUser()),
                                                        m.getDenomination(),
                                                        m.getChurch(),
                                                        specialties,
                                                        m.getBio(),
                                                        m.getYearsExperience(),
                                                        m.isVerified(),
                                                        m.getRating(),
                                                        isMine,
                                                        finalStatus);
                                })
                                .toList();
        }

        /*
         * =========================================================
         * NOTE: POST /api/mentor-requests is now handled by
         * MentorRequestController.send() — which enforces the
         * A1 exclusivity rule (one mentor per member).
         * =========================================================
         */

        /*
         * =========================================================
         * Member — apply to become a mentor
         * =========================================================
         */

        @PostMapping("/mentor-applications")
        public MentorApplication apply(
                        @Valid @RequestBody ApplicationDto r,
                        @AuthenticationPrincipal Jwt jwt) {
                MentorApplication x = new MentorApplication();
                x.setApplicant(users.findById(UUID.fromString(jwt.getSubject())).orElseThrow());
                x.setQualifications(r.qualifications());
                x.setOrganization(r.organization());
                x.setYearsExperience(r.yearsExperience());
                x.setDocumentUrl(r.documentUrl());

                MentorApplication saved = applications.save(x);

                audit.record(
                                AuditAction.MENTOR_APPLICATION_SUBMITTED,
                                "MENTOR_APPLICATION",
                                saved.getId(),
                                "Application submitted by " + x.getApplicant().getEmail());

                return saved;
        }

        /*
         * =========================================================
         * Admin — list pending applications
         * =========================================================
         */

        @GetMapping("/admin/mentor-applications")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        public List<PendingApplicationView> pending() {
                return applications
                                .findByStatusOrderByCreatedAtDesc("PENDING")
                                .stream()
                                .map(x -> new PendingApplicationView(
                                                x.getId().toString(),
                                                x.getApplicant().getId().toString(),
                                                x.getApplicant().getName(),
                                                x.getApplicant().getEmail(),
                                                x.getQualifications(),
                                                Objects.toString(x.getOrganization(), ""),
                                                x.getYearsExperience(),
                                                Objects.toString(x.getDocumentUrl(), ""),
                                                x.getStatus(),
                                                x.getCreatedAt().toString()))
                                .toList();
        }

        /*
         * =========================================================
         * Admin — approve or reject an application
         * =========================================================
         */

        @PutMapping("/admin/mentor-applications/{id}")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        public void review(
                        @PathVariable UUID id,
                        @RequestBody ReviewDto body) {
                MentorApplication application = applications.findById(id).orElseThrow();
                String status = body.status() == null ? "REJECTED" : body.status().toUpperCase();

                application.setStatus(status);
                applications.save(application);

                if ("APPROVED".equals(status)) {
                        AppUser user = application.getApplicant();
                        user.setRole(Role.MENTOR);
                        users.save(user);

                        MentorProfile profile = mentors.findByUserId(user.getId())
                                        .orElseGet(MentorProfile::new);

                        profile.setUser(user);
                        if (profile.getBio() == null) {
                                profile.setBio("Accredited Christian mentor");
                        }
                        profile.setYearsExperience(application.getYearsExperience());
                        profile.setVerified(true);
                        if (profile.getSpecialties() == null) {
                                profile.setSpecialties("Prayer, Bible study, discipleship");
                        }

                        mentors.save(profile);

                        notifications.create(
                                        user.getId(),
                                        null,
                                        NotificationType.MENTOR_ACCEPTED,
                                        "You're now a verified mentor! ✨",
                                        "Your mentor application was approved. You now appear in the mentor directory and can begin accepting mentees.",
                                        "/mentor/requests",
                                        true);

                        audit.record(
                                        AuditAction.MENTOR_APPLICATION_APPROVED,
                                        "MENTOR_APPLICATION",
                                        application.getId(),
                                        "Approved — user promoted to MENTOR" +
                                                        (body.reason() != null ? " — " + body.reason() : ""));
                } else if ("REJECTED".equals(status)) {
                        notifications.create(
                                        application.getApplicant().getId(),
                                        null,
                                        NotificationType.MENTOR_DECLINED,
                                        "Mentor application update",
                                        "Your mentor application was not approved at this time." +
                                                        (body.reason() != null && !body.reason().isBlank()
                                                                        ? " Reason: " + body.reason()
                                                                        : ""),
                                        "/dashboard",
                                        true);

                        audit.record(
                                        AuditAction.MENTOR_APPLICATION_REJECTED,
                                        "MENTOR_APPLICATION",
                                        application.getId(),
                                        "Rejected" + (body.reason() != null ? " — " + body.reason() : ""));
                }
        }

        /*
         * =========================================================
         * Mentor dashboard
         * =========================================================
         */

        /** Own mentor profile. */
        @GetMapping("/mentor/me")
        @PreAuthorize("hasAnyAuthority('ROLE_MENTOR', 'ROLE_ADMIN')")
        public MentorProfileView me(@AuthenticationPrincipal Jwt jwt) {
                MentorProfile m = mentors.findByUserId(UUID.fromString(jwt.getSubject()))
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Mentor profile not found"));
                return toProfileView(m);
        }

        /** Pending requests queue. */
        @GetMapping("/mentor/requests")
        @PreAuthorize("hasAnyAuthority('ROLE_MENTOR', 'ROLE_ADMIN')")
        public List<MentorRequestView> pendingRequests(@AuthenticationPrincipal Jwt jwt) {
                MentorProfile m = mentors.findByUserId(UUID.fromString(jwt.getSubject()))
                                .orElseThrow();

                return requests
                                .findByMentorIdAndStatusOrderByCreatedAtDesc(m.getId(), MentorRequestStatus.PENDING)
                                .stream()
                                .map(this::toRequestView)
                                .toList();
        }

        /** Request history (any non-pending). */
        @GetMapping("/mentor/requests/history")
        @PreAuthorize("hasAnyAuthority('ROLE_MENTOR', 'ROLE_ADMIN')")
        public List<MentorRequestView> requestHistory(@AuthenticationPrincipal Jwt jwt) {
                MentorProfile m = mentors.findByUserId(UUID.fromString(jwt.getSubject()))
                                .orElseThrow();

                return requests
                                .findByMentorIdOrderByCreatedAtDesc(m.getId())
                                .stream()
                                .filter(r -> !MentorRequestStatus.PENDING.equals(r.getStatus()))
                                .map(this::toRequestView)
                                .toList();
        }

        /** Accept a pending request. */
        @PostMapping("/mentor/requests/{id}/accept")
        @PreAuthorize("hasAnyAuthority('ROLE_MENTOR', 'ROLE_ADMIN')")
        public MentorRequestView accept(
                        @PathVariable UUID id,
                        @RequestBody(required = false) AcceptRequest body,
                        @AuthenticationPrincipal Jwt jwt) {

                UUID actorId = UUID.fromString(jwt.getSubject());
                MentorRequest r = mentorRequestService.accept(
                                id, actorId, body != null ? body.note() : null);
                return toRequestView(r);
        }

        /** Decline a pending request. */
        @PostMapping("/mentor/requests/{id}/decline")
        @PreAuthorize("hasAnyAuthority('ROLE_MENTOR', 'ROLE_ADMIN')")
        public MentorRequestView decline(
                        @PathVariable UUID id,
                        @RequestBody(required = false) DeclineRequest body,
                        @AuthenticationPrincipal Jwt jwt) {

                UUID actorId = UUID.fromString(jwt.getSubject());
                MentorRequest r = mentorRequestService.decline(
                                id, actorId, body != null ? body.reason() : null);
                return toRequestView(r);
        }

        /** Active mentees. */
        @GetMapping("/mentor/mentees")
        @PreAuthorize("hasAnyAuthority('ROLE_MENTOR', 'ROLE_ADMIN')")
        public List<MenteeView> mentees(@AuthenticationPrincipal Jwt jwt) {
                MentorProfile m = mentors.findByUserId(UUID.fromString(jwt.getSubject()))
                                .orElseThrow();

                return requests
                                .findByMentorIdAndStatusInOrderByCreatedAtDesc(
                                                m.getId(),
                                                List.of(MentorRequestStatus.ACCEPTED, MentorRequestStatus.COMPLETED))
                                .stream()
                                .map(r -> new MenteeView(
                                                r.getId().toString(),
                                                toUserView(r.getMember()),
                                                r.getRespondedAt() == null ? null : r.getRespondedAt().toString(),
                                                null,
                                                r.getStatus()))
                                .toList();
        }

        /** Impact stats. */
        @GetMapping("/mentor/stats")
        @PreAuthorize("hasAnyAuthority('ROLE_MENTOR', 'ROLE_ADMIN')")
        public MentorStats stats(@AuthenticationPrincipal Jwt jwt) {
                MentorProfile m = mentors.findByUserId(UUID.fromString(jwt.getSubject()))
                                .orElseThrow();

                long active = requests.countByMentorIdAndStatus(m.getId(), MentorRequestStatus.ACCEPTED);
                long pending = requests.countByMentorIdAndStatus(m.getId(), MentorRequestStatus.PENDING);
                long accepted = requests.countByMentorIdAndStatus(m.getId(), MentorRequestStatus.ACCEPTED);
                long completed = requests.countByMentorIdAndStatus(m.getId(), MentorRequestStatus.COMPLETED);

                double rating = m.getRating() == null ? 0d : m.getRating();

                return new MentorStats(
                                active,
                                pending,
                                accepted,
                                completed,
                                rating,
                                m.getYearsExperience(),
                                m.isVerified());
        }

        /*
         * =========================================================
         * Helpers
         * =========================================================
         */

        private MentorProfileView toProfileView(MentorProfile m) {
                List<String> specialties = m.getSpecialties() == null
                                ? List.of()
                                : Arrays.stream(m.getSpecialties().split(","))
                                                .map(String::trim)
                                                .filter(s -> !s.isEmpty())
                                                .toList();

                return new MentorProfileView(
                                m.getId().toString(),
                                toUserView(m.getUser()),
                                m.getDenomination(),
                                m.getChurch(),
                                specialties,
                                m.getBio(),
                                m.getYearsExperience(),
                                m.isVerified(),
                                m.getRating());
        }

        private MentorRequestView toRequestView(MentorRequest r) {
                return new MentorRequestView(
                                r.getId().toString(),
                                toUserView(r.getMember()),
                                r.getMessage(),
                                r.getStatus(),
                                r.getRespondedAt() == null ? null : r.getRespondedAt().toString(),
                                r.getResponseNote(),
                                r.getCreatedAt().toString());
        }

        private AuthDtos.UserView toUserView(AppUser u) {
                return new AuthDtos.UserView(
                                u.getId().toString(),
                                u.getName(),
                                u.getEmail(),
                                u.getRole(),
                                u.getAvatarUrl(),
                                u.getBio(),
                                u.getCreatedAt().toString());
        }
}