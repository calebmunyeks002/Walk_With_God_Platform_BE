package org.walkwithgod.community;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
import org.walkwithgod.auth.AuthDtos;
import org.walkwithgod.user.AppUser;
import org.walkwithgod.user.UserRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

    private final CommunityRepository communities;
    private final CommunityMemberRepository members;
    private final CommunityJoinRequestRepository joinRequests;
    private final CommunityService service;
    private final UserRepository users;

    public CommunityController(
            CommunityRepository c,
            CommunityMemberRepository m,
            CommunityJoinRequestRepository j,
            CommunityService s,
            UserRepository u) {
        communities = c;
        members = m;
        joinRequests = j;
        service = s;
        users = u;
    }

    /*
     * =========================================================
     * DTOs
     * =========================================================
     */

    public record CommunityView(
            String id,
            String name,
            String slug,
            String description,
            String coverImageUrl,
            String iconEmoji,
            String visibility,
            int memberCount,
            int postCount,
            String creatorId,
            String creatorName,
            String createdAt,
            boolean isMember,
            String myRole,
            long pendingRequests) {
    }

    public record CreateCommunity(
            @NotBlank @Size(min = 3, max = 120) String name,
            @Size(max = 2000) String description,
            @Size(max = 1000) String coverImageUrl,
            @Size(max = 20) String iconEmoji,
            CommunityVisibility visibility) {
    }

    public record UpdateCommunity(
            @Size(min = 3, max = 120) String name,
            @Size(max = 2000) String description,
            @Size(max = 1000) String coverImageUrl,
            @Size(max = 20) String iconEmoji,
            CommunityVisibility visibility) {
    }

    public record MemberView(
            String userId,
            String name,
            String email,
            String avatarUrl,
            String role,
            String joinedAt) {
    }

    public record JoinRequestView(
            String id,
            String userId,
            String name,
            String avatarUrl,
            String message,
            String status,
            String createdAt) {
    }

    /*
     * =========================================================
     * List / discover
     * =========================================================
     */

    @GetMapping
    public Page<CommunityView> list(
            @RequestParam(defaultValue = "discover") String mode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {

        UUID uid = UUID.fromString(jwt.getSubject());
        Pageable pr = PageRequest.of(page, size);

        Page<Community> result = "mine".equalsIgnoreCase(mode)
                ? communities.myCommunities(uid, pr)
                : communities.findByHiddenFalseOrderByMemberCountDesc(pr);

        return result.map(c -> toView(c, uid));
    }

    @GetMapping("/{slug}")
    public CommunityView get(
            @PathVariable String slug,
            @AuthenticationPrincipal Jwt jwt) {

        UUID uid = UUID.fromString(jwt.getSubject());
        Community c = communities.findBySlug(slug).orElseThrow();

        // Private communities: only members (and admins) can see the detail
        if (c.getVisibility() == CommunityVisibility.PRIVATE) {
            AppUser u = users.findById(uid).orElseThrow();
            boolean isMember = members.existsByCommunityIdAndUserId(c.getId(), uid);
            boolean isAdmin = u.getRole() == org.walkwithgod.user.Role.ADMIN;
            if (!isMember && !isAdmin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "This community is private");
            }
        }

        if (c.isHidden()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return toView(c, uid);
    }

    /*
     * =========================================================
     * Create / update / delete
     * =========================================================
     */

    @PostMapping
    @Transactional
    public CommunityView create(
            @Valid @RequestBody CreateCommunity r,
            @AuthenticationPrincipal Jwt jwt) {

        UUID uid = UUID.fromString(jwt.getSubject());
        AppUser me = users.findById(uid).orElseThrow();
        service.assertCanCreate(me);

        Community c = new Community();
        c.setName(r.name().trim());
        c.setSlug(service.generateUniqueSlug(r.name()));
        c.setDescription(r.description());
        c.setCoverImageUrl(r.coverImageUrl());
        c.setIconEmoji(r.iconEmoji() != null && !r.iconEmoji().isBlank() ? r.iconEmoji() : "🏛");
        c.setCreatorId(uid);
        c.setVisibility(r.visibility() != null ? r.visibility() : CommunityVisibility.PUBLIC);
        communities.save(c);

        // Creator becomes OWNER
        service.addMember(c.getId(), uid, CommunityRole.OWNER);

        return toView(c, uid);
    }

    @PatchMapping("/{id}")
    @Transactional
    public CommunityView update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCommunity r,
            @AuthenticationPrincipal Jwt jwt) {

        UUID uid = UUID.fromString(jwt.getSubject());
        Community c = communities.findById(id).orElseThrow();
        service.assertCanManage(c, uid);

        if (r.name() != null) {
            c.setName(r.name().trim());
            // Don't change slug — URLs should be stable
        }
        if (r.description() != null)
            c.setDescription(r.description());
        if (r.coverImageUrl() != null)
            c.setCoverImageUrl(r.coverImageUrl());
        if (r.iconEmoji() != null && !r.iconEmoji().isBlank())
            c.setIconEmoji(r.iconEmoji());
        if (r.visibility() != null)
            c.setVisibility(r.visibility());

        communities.save(c);
        return toView(c, uid);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        UUID uid = UUID.fromString(jwt.getSubject());
        Community c = communities.findById(id).orElseThrow();

        AppUser me = users.findById(uid).orElseThrow();
        boolean isAdmin = me.getRole() == org.walkwithgod.user.Role.ADMIN;

        // Only ADMIN or OWNER can delete
        if (!isAdmin) {
            CommunityMember m = members.findByCommunityIdAndUserId(id, uid).orElse(null);
            if (m == null || m.getRole() != CommunityRole.OWNER) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }

        // Soft-delete (keeps posts for audit; posts are orphaned but not hidden)
        c.setHidden(true);
        c.setHiddenReason("Deleted by " + (isAdmin ? "admin" : "owner"));
        c.setHiddenAt(java.time.Instant.now());
        c.setHiddenBy(uid);
        communities.save(c);
    }

    /*
     * =========================================================
     * Join / leave
     * =========================================================
     */

    @PostMapping("/{id}/join")
    @Transactional
    public CommunityView join(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        UUID uid = UUID.fromString(jwt.getSubject());
        Community c = communities.findById(id).orElseThrow();
        if (c.isHidden())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        if (c.getVisibility() == CommunityVisibility.PUBLIC) {
            service.addMember(id, uid, CommunityRole.MEMBER);
        } else {
            // Private → create a join request
            if (!members.existsByCommunityIdAndUserId(id, uid)) {
                joinRequests.findByCommunityIdAndUserId(id, uid).orElseGet(() -> {
                    CommunityJoinRequest r = new CommunityJoinRequest();
                    r.setCommunityId(id);
                    r.setUserId(uid);
                    r.setStatus(JoinRequestStatus.PENDING);
                    return joinRequests.save(r);
                });
            }
        }

        return toView(c, uid);
    }

    @DeleteMapping("/{id}/leave")
    @Transactional
    public void leave(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        UUID uid = UUID.fromString(jwt.getSubject());
        CommunityMember me = members.findByCommunityIdAndUserId(id, uid).orElseThrow();

        // If the last OWNER leaves, promote a MODERATOR, else soft-delete
        if (me.getRole() == CommunityRole.OWNER) {
            List<CommunityMember> all = members.findByCommunityIdOrderByRoleAscJoinedAtAsc(id);
            CommunityMember successor = all.stream()
                    .filter(m -> !m.getUserId().equals(uid))
                    .filter(m -> m.getRole() == CommunityRole.MODERATOR)
                    .findFirst()
                    .orElse(null);

            if (successor != null) {
                successor.setRole(CommunityRole.OWNER);
                members.save(successor);
            } else {
                // No successor → soft-delete the community
                Community c = communities.findById(id).orElseThrow();
                c.setHidden(true);
                c.setHiddenReason("Auto-deleted: owner left with no successor");
                communities.save(c);
            }
        }

        service.removeMember(id, uid);
    }

    /*
     * =========================================================
     * Members
     * =========================================================
     */

    @GetMapping("/{id}/members")
    public List<MemberView> listMembers(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        UUID uid = UUID.fromString(jwt.getSubject());
        Community c = communities.findById(id).orElseThrow();
        service.assertIsMember(id, uid);

        return members.findByCommunityIdOrderByRoleAscJoinedAtAsc(id).stream()
                .map(m -> {
                    AppUser u = users.findById(m.getUserId()).orElseThrow();
                    return new MemberView(
                            u.getId().toString(),
                            u.getName(),
                            u.getEmail(),
                            u.getAvatarUrl(),
                            m.getRole().name(),
                            m.getJoinedAt().toString());
                })
                .toList();
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Transactional
    public void removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID uid = UUID.fromString(jwt.getSubject());
        Community c = communities.findById(id).orElseThrow();
        service.assertCanManage(c, uid);

        // Cannot remove yourself via this endpoint — use /leave
        if (uid.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Use /leave to remove yourself");
        }
        service.removeMember(id, userId);
    }

    /*
     * =========================================================
     * Join requests (PRIVATE communities)
     * =========================================================
     */

    @GetMapping("/{id}/requests")
    public List<JoinRequestView> listRequests(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        UUID uid = UUID.fromString(jwt.getSubject());
        Community c = communities.findById(id).orElseThrow();
        service.assertCanManage(c, uid);

        return joinRequests
                .findByCommunityIdAndStatusOrderByCreatedAtAsc(id, JoinRequestStatus.PENDING)
                .stream()
                .map(r -> {
                    AppUser u = users.findById(r.getUserId()).orElseThrow();
                    return new JoinRequestView(
                            r.getId().toString(),
                            u.getId().toString(),
                            u.getName(),
                            u.getAvatarUrl(),
                            r.getMessage(),
                            r.getStatus().name(),
                            r.getCreatedAt().toString());
                })
                .toList();
    }

    @PostMapping("/{id}/requests/{requestId}/approve")
    @Transactional
    public void approveRequest(
            @PathVariable UUID id,
            @PathVariable UUID requestId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID uid = UUID.fromString(jwt.getSubject());
        Community c = communities.findById(id).orElseThrow();
        service.assertCanManage(c, uid);

        CommunityJoinRequest r = joinRequests.findById(requestId).orElseThrow();
        if (!r.getCommunityId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        r.setStatus(JoinRequestStatus.APPROVED);
        r.setReviewedBy(uid);
        r.setReviewedAt(java.time.Instant.now());
        joinRequests.save(r);

        service.addMember(id, r.getUserId(), CommunityRole.MEMBER);
    }

    @PostMapping("/{id}/requests/{requestId}/reject")
    @Transactional
    public void rejectRequest(
            @PathVariable UUID id,
            @PathVariable UUID requestId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID uid = UUID.fromString(jwt.getSubject());
        Community c = communities.findById(id).orElseThrow();
        service.assertCanManage(c, uid);

        CommunityJoinRequest r = joinRequests.findById(requestId).orElseThrow();
        if (!r.getCommunityId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        r.setStatus(JoinRequestStatus.REJECTED);
        r.setReviewedBy(uid);
        r.setReviewedAt(java.time.Instant.now());
        joinRequests.save(r);
    }

    /*
     * =========================================================
     * Mapping
     * =========================================================
     */

    private CommunityView toView(Community c, UUID viewerId) {
        AppUser creator = users.findById(c.getCreatorId()).orElse(null);
        CommunityMember mine = members.findByCommunityIdAndUserId(c.getId(), viewerId).orElse(null);
        long pending = c.getVisibility() == CommunityVisibility.PRIVATE
                ? joinRequests.countByCommunityIdAndStatus(c.getId(), JoinRequestStatus.PENDING)
                : 0;

        return new CommunityView(
                c.getId().toString(),
                c.getName(),
                c.getSlug(),
                c.getDescription(),
                c.getCoverImageUrl(),
                c.getIconEmoji(),
                c.getVisibility().name(),
                c.getMemberCount(),
                c.getPostCount(),
                c.getCreatorId().toString(),
                creator != null ? creator.getName() : "Unknown",
                c.getCreatedAt().toString(),
                mine != null,
                mine != null ? mine.getRole().name() : null,
                pending);
    }
}