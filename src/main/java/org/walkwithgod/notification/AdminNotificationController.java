package org.walkwithgod.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.walkwithgod.audit.AuditAction;
import org.walkwithgod.audit.AuditService;
import org.walkwithgod.user.AppUser;
import org.walkwithgod.user.Role;
import org.walkwithgod.user.UserRepository;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/notifications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    private final NotificationRepository repo;
    private final UserRepository users;
    private final NotificationService service;
    private final AuditService audit;

    public AdminNotificationController(
            NotificationRepository repo,
            UserRepository users,
            NotificationService service,
            AuditService audit) {
        this.repo = repo;
        this.users = users;
        this.service = service;
        this.audit = audit;
    }

    /*
     * =========================================================
     * Send
     * =========================================================
     */

    public record SendRequest(
            @NotBlank String target, // USER | ROLE | ALL
            String userId, // required if target=USER
            String role, // required if target=ROLE
            @NotBlank @Size(max = 200) String title,
            @Size(max = 2000) String body,
            @Size(max = 500) String link,
            Boolean sendEmail) {
    }

    @PostMapping("/send")
    @Transactional
    public SendResult send(
            @Valid @RequestBody SendRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        UUID senderId = UUID.fromString(jwt.getSubject());
        boolean email = req.sendEmail() != null && req.sendEmail();

        List<AppUser> recipients;
        switch (req.target().toUpperCase()) {
            case "USER" -> {
                if (req.userId() == null)
                    throw new IllegalArgumentException("userId is required");
                recipients = List.of(
                        users.findById(UUID.fromString(req.userId())).orElseThrow());
            }
            case "ROLE" -> {
                if (req.role() == null)
                    throw new IllegalArgumentException("role is required");
                recipients = users.findAll().stream()
                        .filter(u -> u.getRole().name().equalsIgnoreCase(req.role()))
                        .toList();
            }
            case "ALL" -> recipients = users.findAll();
            default -> throw new IllegalArgumentException("Invalid target");
        }

        int sent = 0;
        for (AppUser u : recipients) {
            service.create(u.getId(), senderId, NotificationType.ADMIN_BROADCAST,
                    req.title(), req.body(), req.link(), email);
            sent++;
        }

        audit.record(AuditAction.SETTING_UPDATED, "NOTIFICATION", null,
                "Admin broadcast to " + req.target() + " (" + sent + " recipients) — " + req.title());

        return new SendResult(sent);
    }

    public record SendResult(int recipients) {
    }

    /*
     * =========================================================
     * List all (with filters)
     * =========================================================
     */

    public record AdminNotificationView(
            String id,
            String recipientId,
            String recipientName,
            String recipientEmail,
            String senderId,
            String type,
            String title,
            String body,
            String link,
            boolean read,
            boolean emailSent,
            String readAt,
            String createdAt) {
    }

    public record PageResponse<T>(
            List<T> content, long totalElements, int totalPages,
            int number, int size, boolean first, boolean last) {
    }

    @GetMapping
    public PageResponse<AdminNotificationView> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false) String recipientId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        final String s = (search == null || search.isBlank()) ? null : search.trim().toLowerCase();
        final UUID recipId = recipientId == null || recipientId.isBlank()
                ? null
                : UUID.fromString(recipientId);
        final NotificationType typeEnum = type == null || type.isBlank() || "ALL".equalsIgnoreCase(type)
                ? null
                : NotificationType.valueOf(type.toUpperCase());
        final java.time.Instant fromI = from == null ? null : java.time.Instant.parse(from);
        final java.time.Instant toI = to == null ? null : java.time.Instant.parse(to);

        Specification<Notification> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (typeEnum != null)
                predicates.add(cb.equal(root.get("type"), typeEnum));
            if (read != null)
                predicates.add(cb.equal(root.get("read"), read));
            if (recipId != null)
                predicates.add(cb.equal(root.get("recipientId"), recipId));
            if (fromI != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromI));
            if (toI != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toI));
            if (s != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), "%" + s + "%"),
                        cb.like(cb.lower(root.get("body")), "%" + s + "%")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> result = repo.findAll(spec, pr);

        var content = result.getContent().stream().map(n -> {
            AppUser r = users.findById(n.getRecipientId()).orElse(null);
            return new AdminNotificationView(
                    n.getId().toString(),
                    n.getRecipientId().toString(),
                    r == null ? "Unknown" : r.getName(),
                    r == null ? "Unknown" : r.getEmail(),
                    n.getSenderId() == null ? null : n.getSenderId().toString(),
                    n.getType().name(),
                    n.getTitle(),
                    n.getBody(),
                    n.getLink(),
                    n.isRead(),
                    n.isEmailSent(),
                    n.getReadAt() == null ? null : n.getReadAt().toString(),
                    n.getCreatedAt().toString());
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
}