package org.walkwithgod.community;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.walkwithgod.user.AppUser;
import org.walkwithgod.user.Role;
import org.walkwithgod.user.UserRepository;

import java.util.Locale;
import java.util.UUID;

@Service
public class CommunityService {

    private final CommunityRepository communities;
    private final CommunityMemberRepository members;
    private final CommunityJoinRequestRepository joinRequests;
    private final UserRepository users;

    public CommunityService(
            CommunityRepository c,
            CommunityMemberRepository m,
            CommunityJoinRequestRepository j,
            UserRepository u) {
        communities = c;
        members = m;
        joinRequests = j;
        users = u;
    }
    
    // In CommunityService.java
    public Community getById(UUID id) {
        return communities.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /*
     * =========================================================
     * Slug helpers
     * =========================================================
     */

    public String generateUniqueSlug(String name) {
        String base = name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isBlank())
            base = "community";
        String slug = base;
        int n = 2;
        while (communities.existsBySlug(slug)) {
            slug = base + "-" + n++;
        }
        return slug;
    }

    /*
     * =========================================================
     * Permissions
     * =========================================================
     */

    /** Admin OR Mentor can create communities. */
    public void assertCanCreate(AppUser user) {
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.MENTOR)
            return;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Only admins and mentors can create communities");
    }

    /** Admin, OWNER, or MODERATOR can manage. */
    public void assertCanManage(Community c, UUID userId) {
        AppUser u = users.findById(userId).orElseThrow();
        if (u.getRole() == Role.ADMIN)
            return;

        CommunityMember m = members.findByCommunityIdAndUserId(c.getId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        if (m.getRole() == CommunityRole.OWNER || m.getRole() == CommunityRole.MODERATOR)
            return;

        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    /** Current user must be a member of the community. */
    public void assertIsMember(UUID communityId, UUID userId) {
        if (!members.existsByCommunityIdAndUserId(communityId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not a member of this community");
        }
    }

    /*
     * =========================================================
     * Join / leave
     * =========================================================
     */

    @Transactional
    public void addMember(UUID communityId, UUID userId, CommunityRole role) {
        if (members.existsByCommunityIdAndUserId(communityId, userId))
            return;

        CommunityMember m = new CommunityMember();
        m.setCommunityId(communityId);
        m.setUserId(userId);
        m.setRole(role);
        members.save(m);

        Community c = communities.findById(communityId).orElseThrow();
        c.setMemberCount((int) members.countByCommunityId(communityId));
        communities.save(c);
    }

    @Transactional
    public void removeMember(UUID communityId, UUID userId) {
        members.findByCommunityIdAndUserId(communityId, userId).ifPresent(m -> {
            members.delete(m);

            Community c = communities.findById(communityId).orElseThrow();
            c.setMemberCount((int) members.countByCommunityId(communityId));

            // If no members left, soft-delete the community
            if (c.getMemberCount() == 0) {
                c.setHidden(true);
                c.setHiddenReason("Auto-deleted: last member left");
            }
            communities.save(c);
        });
    }

    /*
     * =========================================================
     * Post count
     * =========================================================
     */

    @Transactional
    public void incrementPostCount(UUID communityId) {
        communities.findById(communityId).ifPresent(c -> {
            c.setPostCount(c.getPostCount() + 1);
            communities.save(c);
        });
    }
}