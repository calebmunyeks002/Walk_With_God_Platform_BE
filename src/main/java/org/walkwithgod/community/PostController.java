package org.walkwithgod.community;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.walkwithgod.auth.AuthDtos;
import org.walkwithgod.media.MediaRepository;
import org.walkwithgod.user.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

        private final PostRepository posts;
        private final ReactionRepository reactions;
        private final CommentRepository comments;
        private final UserRepository users;
        private final MediaRepository mediaRepository;
        private final CommunityService communityService;

        public PostController(
                        PostRepository p,
                        ReactionRepository r,
                        CommentRepository c,
                        UserRepository u,
                        MediaRepository mediaRepository,
                        CommunityService communityService) {
                this.posts = p;
                this.reactions = r;
                this.comments = c;
                this.users = u;
                this.mediaRepository = mediaRepository;
                this.communityService = communityService;
        }

        /*
         * =========================================================
         * DTOs
         * =========================================================
         */

        public record CreatePost(
                        @NotBlank @Size(max = 5000) String content,
                        @Size(max = 160) String scriptureReference,
                        @Size(max = 1000) String imageUrl,
                        PostType type,
                        UUID mediaId,
                        UUID communityId) {
        }

        public record ReactionRequest(@NotBlank String type) {
        }

        public record ShareRequest(@Size(max = 5000) String content) {
        }

        public record ReactionBreakdown(
                        long like, long love, long amen, long pray, long total) {
        }

        public record PostView(
                        String id,
                        AuthDtos.UserView author,
                        String content,
                        String scriptureReference,
                        String imageUrl,
                        PostType type,
                        UUID mediaId,
                        String mediaType,
                        UUID sharedFromId, // ← shared comes first
                        UUID communityId, // ← community second
                        PostView sharedFrom,
                        ReactionBreakdown reactionBreakdown,
                        String myReaction,
                        long comments,
                        String createdAt) {
        }

        /*
         * =========================================================
         * Feed with filters
         * =========================================================
         */

        @GetMapping
        public Page<PostView> list(
                        @RequestParam(defaultValue = "ALL") String filter,
                        Pageable pageable,
                        @AuthenticationPrincipal Jwt jwt) {

                UUID uid = UUID.fromString(jwt.getSubject());
                Page<Post> page;

                switch (filter.toUpperCase()) {
                        case "FOLLOWING" -> page = posts.followingFeedGlobal(uid, pageable);
                        case "MENTORS" -> page = posts.mentorFeed(pageable);
                        case "PRAYER" -> page = posts.findByTypeAndHiddenFalseOrderByCreatedAtDesc(
                                        PostType.PRAYER, pageable);
                        case "MY_COMMUNITIES" -> page = posts.myCommunitiesFeed(uid, pageable);
                        default -> page = posts.globalFeed(pageable);
                }

                return page.map(p -> toView(p, uid, true));
        }

        /*
         * =========================================================
         * Create post
         * =========================================================
         */

        @PostMapping
        @Transactional
        public PostView create(
                        @Valid @RequestBody CreatePost r,
                        @AuthenticationPrincipal Jwt jwt) {

                UUID uid = UUID.fromString(jwt.getSubject());

                // If posting to a community, user must be a member
                if (r.communityId() != null) {
                        communityService.assertIsMember(r.communityId(), uid);
                }

                Post p = new Post();
                p.setAuthor(users.findById(uid).orElseThrow());
                p.setContent(r.content());
                p.setScriptureReference(r.scriptureReference());
                p.setImageUrl(r.imageUrl());
                p.setMediaId(r.mediaId());
                p.setCommunityId(r.communityId());
                p.setType(r.type() != null ? r.type() : PostType.NORMAL);
                Post saved = posts.save(p);

                if (r.communityId() != null) {
                        communityService.incrementPostCount(r.communityId());
                }
                return toView(saved, uid, false);
        }

        /*
         * =========================================================
         * Delete own post
         * =========================================================
         */

        @DeleteMapping("/{id}")
        @Transactional
        public void deletePost(
                        @PathVariable UUID id,
                        @AuthenticationPrincipal Jwt jwt) {
                UUID uid = UUID.fromString(jwt.getSubject());
                Post p = posts.findById(id).orElseThrow();
                if (!p.getAuthor().getId().equals(uid)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                }
                posts.delete(p);
        }

        /*
         * =========================================================
         * Share — create a new post that references the original
         * =========================================================
         */

        @PostMapping("/{id}/share")
        @Transactional
        public PostView share(
                        @PathVariable UUID id,
                        @RequestBody(required = false) ShareRequest r,
                        @AuthenticationPrincipal Jwt jwt) {

                UUID uid = UUID.fromString(jwt.getSubject());
                Post original = posts.findById(id).orElseThrow();
                if (original.isHidden()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                }

                Post share = new Post();
                share.setAuthor(users.findById(uid).orElseThrow());
                share.setContent(r != null && r.content() != null ? r.content() : "");
                share.setType(PostType.SHARED);
                share.setSharedFromId(original.getId());
                Post saved = posts.save(share);
                return toView(saved, uid, true);
        }

        /*
         * =========================================================
         * Reactions — upsert / remove
         * =========================================================
         */

        @PostMapping("/{id}/reactions")
        @Transactional
        public ReactionBreakdown react(
                        @PathVariable UUID id,
                        @Valid @RequestBody ReactionRequest r,
                        @AuthenticationPrincipal Jwt jwt) {

                UUID uid = UUID.fromString(jwt.getSubject());
                Post p = posts.findById(id).orElseThrow();

                if (p.isHidden()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                }

                String type = r.type().toUpperCase();
                if (!Set.of("LIKE", "LOVE", "AMEN", "PRAY").contains(type)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown reaction");
                }

                Reaction existing = reactions.findByUserIdAndPostId(uid, id).orElse(null);

                if (existing != null && existing.getType().equals(type)) {
                        // Same emoji → un-react
                        reactions.delete(existing);
                } else if (existing != null) {
                        // Change emoji
                        existing.setType(type);
                        reactions.save(existing);
                } else {
                        Reaction n = new Reaction();
                        n.setUser(users.findById(uid).orElseThrow());
                        n.setPost(p);
                        n.setType(type);
                        reactions.save(n);
                }
                return breakdownFor(id);
        }

        @DeleteMapping("/{id}/reactions")
        @Transactional
        public ReactionBreakdown unreact(
                        @PathVariable UUID id,
                        @AuthenticationPrincipal Jwt jwt) {
                UUID uid = UUID.fromString(jwt.getSubject());
                reactions.findByUserIdAndPostId(uid, id).ifPresent(reactions::delete);
                return breakdownFor(id);
        }

        private ReactionBreakdown breakdownFor(UUID postId) {
                Map<String, Long> m = new HashMap<>();
                for (Object[] row : reactions.countsByTypeForPost(postId)) {
                        m.put((String) row[0], (Long) row[1]);
                }
                long like = m.getOrDefault("LIKE", 0L);
                long love = m.getOrDefault("LOVE", 0L);
                long amen = m.getOrDefault("AMEN", 0L);
                long pray = m.getOrDefault("PRAY", 0L);
                return new ReactionBreakdown(like, love, amen, pray, like + love + amen + pray);
        }

        /*
         * =========================================================
         * Single post
         * =========================================================
         */

        @GetMapping("/{id}")
        public PostView get(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
                UUID uid = UUID.fromString(jwt.getSubject());
                Post p = posts.findById(id).orElseThrow();
                if (p.isHidden()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                }
                return toView(p, uid, true);
        }

        @GetMapping("/community/{communityId}")
        public Page<PostView> listByCommunity(
                        @PathVariable UUID communityId,
                        Pageable pageable,
                        @AuthenticationPrincipal Jwt jwt) {

                UUID uid = UUID.fromString(jwt.getSubject());

                // Optional: enforce membership for private communities
                Community c = communityService.getById(communityId); // you'll need this helper
                if (c.getVisibility() == CommunityVisibility.PRIVATE) {
                        communityService.assertIsMember(communityId, uid);
                }

                return posts.findByCommunityIdAndHiddenFalseOrderByCreatedAtDesc(communityId, pageable)
                                .map(p -> toView(p, uid, true));
        }

        /*
         * =========================================================
         * Mapping
         * =========================================================
         */

        private PostView toView(Post p, UUID viewerId, boolean includeShared) {

                /* ---- Author (7 fields exactly) ---- */
                AuthDtos.UserView author = new AuthDtos.UserView(
                                p.getAuthor().getId().toString(),
                                p.getAuthor().getName(),
                                p.getAuthor().getEmail(),
                                p.getAuthor().getRole(),
                                p.getAuthor().getAvatarUrl(),
                                p.getAuthor().getBio(),
                                p.getAuthor().getCreatedAt().toString());

                /* ---- Shared origin (recursive, non-looping) ---- */
                PostView sharedFrom = null;
                if (includeShared && p.getSharedFromId() != null) {
                        Post orig = posts.findById(p.getSharedFromId()).orElse(null);
                        if (orig != null && !orig.isHidden()) {
                                sharedFrom = toView(orig, viewerId, false);
                        }
                }

                /* ---- Viewer's own reaction ---- */
                Reaction mine = reactions.findByUserIdAndPostId(viewerId, p.getId()).orElse(null);

                /* ---- Media type (IMAGE/VIDEO) resolved from MediaRepository ---- */
                String mediaType = null;
                if (p.getMediaId() != null) {
                        mediaType = mediaRepository.findById(p.getMediaId())
                                        .map(m -> m.getType().name())
                                        .orElse(null);
                }

                /* ---- Final view — ARGUMENT ORDER MUST MATCH PostView RECORD ---- */
                return new PostView(
                                p.getId().toString(),
                                author,
                                p.getContent(),
                                p.getScriptureReference(),
                                p.getImageUrl(),
                                p.getType(),
                                p.getMediaId(),
                                mediaType,
                                p.getSharedFromId(), // ← slot 9
                                p.getCommunityId(), // ← slot 10
                                sharedFrom, // ← slot 11
                                breakdownFor(p.getId()),
                                mine != null ? mine.getType() : null,
                                comments.countByPostIdAndDeletedFalse(p.getId()),
                                p.getCreatedAt().toString());
        }
}