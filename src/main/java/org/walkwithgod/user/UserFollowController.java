package org.walkwithgod.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserFollowController {

    private final UserFollowRepository follows;
    private final UserRepository users;

    public UserFollowController(UserFollowRepository f, UserRepository u) {
        follows = f;
        users = u;
    }

    public record FollowStats(long followers, long following, boolean followedByMe) {
    }

    @PostMapping("/{id}/follow")
    @Transactional
    public FollowStats follow(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID me = UUID.fromString(jwt.getSubject());
        if (me.equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot follow yourself");
        }
        users.findById(id).orElseThrow();

        if (!follows.existsByFollowerIdAndFollowedId(me, id)) {
            UserFollow f = new UserFollow();
            f.setFollowerId(me);
            f.setFollowedId(id);
            follows.save(f);
        }
        return statsFor(id, me);
    }

    @DeleteMapping("/{id}/follow")
    @Transactional
    public FollowStats unfollow(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID me = UUID.fromString(jwt.getSubject());
        follows.findByFollowerIdAndFollowedId(me, id).ifPresent(follows::delete);
        return statsFor(id, me);
    }

    @GetMapping("/{id}/follow")
    public FollowStats status(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID me = UUID.fromString(jwt.getSubject());
        return statsFor(id, me);
    }

    private FollowStats statsFor(UUID userId, UUID viewer) {
        long followers = follows.findByFollowedId(userId).size();
        long following = follows.findByFollowerId(userId).size();
        boolean followed = follows.existsByFollowerIdAndFollowedId(viewer, userId);
        return new FollowStats(followers, following, followed);
    }
}