package org.walkwithgod.media;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/media")
public class AdminMediaController {

    private final MediaRepository repo;
    private final MediaService media;

    public AdminMediaController(MediaRepository repo, MediaService media) {
        this.repo = repo;
        this.media = media;
    }

    public record AdminMediaView(
            String id, String uploaderId,
            String type, String status,
            String originalFilename, String contentType,
            long sizeBytes, String checksumSha256,
            String flaggedReason, String createdAt) {
    }

    public record ReviewRequest(String reason) {
    }

    @GetMapping
    public Page<AdminMediaView> list(
            @RequestParam(defaultValue = "FLAGGED") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ModerationStatus st = ModerationStatus.valueOf(status.toUpperCase());
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        return repo.findByStatusOrderByCreatedAtAsc(st, pr).map(this::toView);
    }

    @GetMapping("/counts")
    public java.util.Map<String, Long> counts() {
        return java.util.Map.of(
                "flagged", repo.countByStatus(ModerationStatus.FLAGGED),
                "pending", repo.countByStatus(ModerationStatus.PENDING),
                "rejected", repo.countByStatus(ModerationStatus.REJECTED));
    }

    @PostMapping("/{id}/approve")
    @Transactional
    public AdminMediaView approve(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        Media m = media.get(id);
        m.setStatus(ModerationStatus.APPROVED);
        m.setFlaggedReason(null);
        m.setReviewedBy(UUID.fromString(jwt.getSubject()));
        m.setReviewedAt(Instant.now());
        repo.save(m);
        return toView(m);
    }

    @PostMapping("/{id}/reject")
    @Transactional
    public AdminMediaView reject(
            @PathVariable UUID id,
            @RequestBody(required = false) ReviewRequest req,
            @AuthenticationPrincipal Jwt jwt) {

        Media m = media.get(id);
        m.setStatus(ModerationStatus.REJECTED);
        m.setFlaggedReason(req != null ? req.reason() : "Rejected by moderator");
        m.setReviewedBy(UUID.fromString(jwt.getSubject()));
        m.setReviewedAt(Instant.now());
        repo.save(m);
        return toView(m);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void hardDelete(@PathVariable UUID id) {
        Media m = media.get(id);
        media.markDeleted(m);
    }

    private AdminMediaView toView(Media m) {
        return new AdminMediaView(
                m.getId().toString(),
                m.getUploaderId().toString(),
                m.getType().name(),
                m.getStatus().name(),
                m.getOriginalFilename(),
                m.getContentType(),
                m.getSizeBytes(),
                m.getChecksumSha256(),
                m.getFlaggedReason(),
                m.getCreatedAt().toString());
    }
}