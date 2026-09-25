package org.walkwithgod.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    public record NotificationView(
            String id,
            String type,
            String title,
            String body,
            String link,
            boolean read,
            String createdAt) {
    }

    public record PageResponse<T>(
            List<T> content, long totalElements, int totalPages,
            int number, int size, boolean first, boolean last) {
    }

    @GetMapping
    public PageResponse<NotificationView> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID uid = UUID.fromString(jwt.getSubject());
        Page<Notification> result = service.listForUser(uid, PageRequest.of(page, size));

        return new PageResponse<>(
                result.getContent().stream().map(this::toView).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize(),
                result.isFirst(),
                result.isLast());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        return Map.of("count", service.unreadCount(uid));
    }

    @PostMapping("/{id}/read")
    public void markRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        service.markRead(id, uid);
    }

    @PostMapping("/mark-all-read")
    public Map<String, Integer> markAllRead(@AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        int updated = service.markAllRead(uid);
        return Map.of("updated", updated);
    }

    private NotificationView toView(Notification n) {
        return new NotificationView(
                n.getId().toString(),
                n.getType().name(),
                n.getTitle(),
                n.getBody(),
                n.getLink(),
                n.isRead(),
                n.getCreatedAt().toString());
    }
}