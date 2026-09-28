package org.walkwithgod.community;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.walkwithgod.auth.AuthDtos;
import org.walkwithgod.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final PostRepository posts;
    private final CommentRepository comments;
    private final UserRepository users;

    public CommentController(PostRepository p, CommentRepository c, UserRepository u) {
        posts = p;
        comments = c;
        users = u;
    }

    public record CreateComment(
            @NotBlank @Size(max = 2000) String content) {
    }

    public record CommentView(
            String id,
            AuthDtos.UserView author,
            String content,
            String createdAt) {
    }

    @GetMapping
    public List<CommentView> list(@PathVariable UUID postId) {
        Post p = posts.findById(postId).orElseThrow();
        if (p.isHidden())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return comments.findByPostIdAndDeletedFalseOrderByCreatedAtAsc(postId)
                .stream().map(this::toView).toList();
    }

    @PostMapping
    @Transactional
    public CommentView create(
            @PathVariable UUID postId,
            @Valid @RequestBody CreateComment r,
            @AuthenticationPrincipal Jwt jwt) {

        Post p = posts.findById(postId).orElseThrow();
        if (p.isHidden())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        Comment c = new Comment();
        c.setPost(p);
        c.setAuthor(users.findById(UUID.fromString(jwt.getSubject())).orElseThrow());
        c.setContent(r.content());
        c = comments.save(c);
        return toView(c);
    }

    @DeleteMapping("/{commentId}")
    @Transactional
    public void delete(
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID uid = UUID.fromString(jwt.getSubject());
        Comment c = comments.findByIdAndDeletedFalse(commentId).orElseThrow();

        if (!c.getPost().getId().equals(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        boolean isAuthor = c.getAuthor().getId().equals(uid);
        boolean isAdmin = users.findById(uid)
                .map(u -> "ADMIN".equals(u.getRole()))
                .orElse(false);

        if (!isAuthor && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        c.setDeleted(true);
        comments.save(c);
    }

    private CommentView toView(Comment c) {
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
}