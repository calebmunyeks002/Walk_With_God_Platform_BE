package org.walkwithgod.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/admin/communities")
public class AdminCommunityController {

    private final CommunityRepository repo;

    public AdminCommunityController(CommunityRepository r) {
        repo = r;
    }

    public record AdminCommunityView(
            String id, String name, String slug,
            String iconEmoji, String visibility,
            int memberCount, int postCount,
            boolean hidden, String hiddenReason,
            String creatorId, String createdAt) {
    }

    public record HideRequest(String reason) {
    }

    @GetMapping
    public Page<AdminCommunityView> list(
            @RequestParam(required = false) Boolean hidden,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Community> result = hidden != null
                ? repo.findAll((root, q, cb) -> cb.equal(root.get("hidden"), hidden), pr)
                : repo.findAll(pr);
        return result.map(this::toView);
    }

    @PostMapping("/{id}/hide")
    @Transactional
    public AdminCommunityView hide(
            @PathVariable UUID id,
            @RequestBody(required = false) HideRequest r,
            @AuthenticationPrincipal Jwt jwt) {

        Community c = repo.findById(id).orElseThrow();
        c.setHidden(true);
        c.setHiddenReason(r != null ? r.reason() : "Hidden by admin");
        c.setHiddenAt(Instant.now());
        c.setHiddenBy(UUID.fromString(jwt.getSubject()));
        repo.save(c);
        return toView(c);
    }

    @PostMapping("/{id}/restore")
    @Transactional
    public AdminCommunityView restore(@PathVariable UUID id) {
        Community c = repo.findById(id).orElseThrow();
        c.setHidden(false);
        c.setHiddenReason(null);
        c.setHiddenAt(null);
        c.setHiddenBy(null);
        repo.save(c);
        return toView(c);
    }

    private AdminCommunityView toView(Community c) {
        return new AdminCommunityView(
                c.getId().toString(),
                c.getName(),
                c.getSlug(),
                c.getIconEmoji(),
                c.getVisibility().name(),
                c.getMemberCount(),
                c.getPostCount(),
                c.isHidden(),
                c.getHiddenReason(),
                c.getCreatorId().toString(),
                c.getCreatedAt().toString());
    }
}