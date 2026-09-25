package org.walkwithgod.community;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.walkwithgod.auth.AuthDtos;
import org.walkwithgod.user.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository posts;
    private final ReactionRepository reactions;
    private final UserRepository users;
    private final CommentRepository comments;

    public PostController(
            PostRepository p,
            ReactionRepository r,
            UserRepository u,
            CommentRepository c) {
        posts = p;
        reactions = r;
        users = u;
        comments = c;
    }

    /*
     * =========================================================
     * DTOs
     * =========================================================
     */

    public record CreatePost(
            @NotBlank @Size(max = 5000) String content,
            @Size(max = 160) String scriptureReference) {
    }

    public record ReactionRequest(@NotBlank String type) {
    }

    public record CommentRequest(@NotBlank @Size(max = 2000) String content) {
    }

    public record CommentView(
            String id,
            AuthDtos.UserView author,
            String content,
            String createdAt) {
    }

    public record PostView(
            String id,
            AuthDtos.UserView author,
            String content,
            String scriptureReference,
            String imageUrl,
            long reactions,
            long comments,
            boolean reacted,
            String createdAt) {
    }

    /*
     * =========================================================
     * Public feed
     * =========================================================
     */

    @GetMapping
    public Page<PostView> list(Pageable pageable, @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        return posts.findByHiddenFalseOrderByCreatedAtDesc(pageable)
                .map(p -> toView(p, uid));
    }

    @PostMapping
    public PostView create(
            @Valid @RequestBody CreatePost r,
            @AuthenticationPrincipal Jwt jwt) {
        Post p = new Post();
        p.setAuthor(users.findById(UUID.fromString(jwt.getSubject())).orElseThrow());
        p.setContent(r.content());
        p.setScriptureReference(r.scriptureReference());
        Post saved = posts.save(p);
        return toView(saved, saved.getAuthor().getId());
    }

    /*
     * =========================================================
     * Comments
     * =========================================================
     */

    @GetMapping("/{id}/comments")
    public List<CommentView> comments(@PathVariable UUID id) {
        Post p = posts.findById(id).orElseThrow();
        if (p.isHidden()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        return comments.findByPostIdOrderByCreatedAtAsc(id).stream()
                .map(c -> new CommentView(
                        c.getId().toString(),
                        new AuthDtos.UserView(
                                c.getAuthor().getId().toString(),
                                c.getAuthor().getName(),
                                c.getAuthor().getEmail(),
                                c.getAuthor().getRole(),
                                c.getAuthor().getAvatarUrl(),
                                c.getAuthor().getBio(),
                                c.getAuthor().getCreatedAt().toString()),
                        c.getContent(),
                        c.getCreatedAt().toString()))
                .toList();
    }

    @PostMapping("/{id}/comments")
    public CommentView comment(
            @PathVariable UUID id,
            @Valid @RequestBody CommentRequest r,
            @AuthenticationPrincipal Jwt jwt) {
        Post p = posts.findById(id).orElseThrow();
        if (p.isHidden()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }

        Comment c = new Comment();
        c.setPost(p);
        c.setAuthor(users.findById(UUID.fromString(jwt.getSubject())).orElseThrow());
        c.setContent(r.content());
        c = comments.save(c);

        return new CommentView(
                c.getId().toString(),
                new AuthDtos.UserView(
                        c.getAuthor().getId().toString(),
                        c.getAuthor().getName(),
                        c.getAuthor().getEmail(),
                        c.getAuthor().getRole(),
                        c.getAuthor().getAvatarUrl(),
                        c.getAuthor().getBio(),
                        c.getAuthor().getCreatedAt().toString()),
                c.getContent(),
                c.getCreatedAt().toString());
    }

    /*
     * =========================================================
     * Reactions
     * =========================================================
     */

    @PostMapping("/{id}/reactions")
    public void react(
            @PathVariable UUID id,
            @Valid @RequestBody ReactionRequest r,
            @AuthenticationPrincipal Jwt jwt) {
        Post p = posts.findById(id).orElseThrow();
        if (p.isHidden()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }

        UUID uid = UUID.fromString(jwt.getSubject());
        Reaction x = reactions.findByUserIdAndPostId(uid, id).orElseGet(() -> {
            Reaction n = new Reaction();
            n.setUser(users.findById(uid).orElseThrow());
            n.setPost(p);
            return n;
        });
        x.setType(r.type());
        reactions.save(x);
    }

    /*
     * =========================================================
     * Helpers
     * =========================================================
     */

    private PostView toView(Post p, UUID uid) {
        return new PostView(
                p.getId().toString(),
                new AuthDtos.UserView(
                        p.getAuthor().getId().toString(),
                        p.getAuthor().getName(),
                        p.getAuthor().getEmail(),
                        p.getAuthor().getRole(),
                        p.getAuthor().getAvatarUrl(),
                        p.getAuthor().getBio(),
                        p.getAuthor().getCreatedAt().toString()),
                p.getContent(),
                p.getScriptureReference(),
                p.getImageUrl(),
                reactions.countByPostId(p.getId()),
                comments.countByPostId(p.getId()),
                reactions.findByUserIdAndPostId(uid, p.getId()).isPresent(),
                p.getCreatedAt().toString());
    }
}