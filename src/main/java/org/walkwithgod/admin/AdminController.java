package org.walkwithgod.admin;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.walkwithgod.moderation.ReportStatus;
import org.walkwithgod.moderation.ReportTargetType;
import org.walkwithgod.user.Role;

import static org.walkwithgod.admin.AdminDtos.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService admin;

    public AdminController(AdminService admin) {
        this.admin = admin;
    }

    /* ---------- Stats ---------- */

    @GetMapping("/stats")
    public SystemStats stats() {
        return admin.stats();
    }

    /* ---------- Users ---------- */

    @GetMapping("/users")
    public PageResponse<AdminUserView> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return admin.listUsers(search, role, enabled, page, size);
    }

    @PostMapping("/users/{id}/suspend")
    public AdminUserView suspend(
            @PathVariable String id,
            @Valid @RequestBody SuspendRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        return admin.suspend(id, req.reason(), jwt);
    }

    @PostMapping("/users/{id}/reactivate")
    public AdminUserView reactivate(
            @PathVariable String id,
            @RequestBody(required = false) SuspendRequest req) {
        return admin.reactivate(id, req == null ? null : req.reason());
    }

    @PatchMapping("/users/{id}/role")
    public AdminUserView changeRole(
            @PathVariable String id,
            @Valid @RequestBody RoleChangeRequest req) {
        return admin.changeRole(id, req.role(), req.reason());
    }

    /* ---------- Posts moderation ---------- */

    @GetMapping("/posts")
    public PageResponse<AdminPostView> listPosts(
            @RequestParam(required = false) Boolean hidden,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return admin.listPosts(hidden, search, page, size);
    }

    @PostMapping("/posts/{id}/delete")
    public AdminPostView deletePost(
            @PathVariable String id,
            @Valid @RequestBody DeletePostRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        return admin.deletePost(id, req.reason(), jwt);
    }

    @PostMapping("/posts/{id}/restore")
    public AdminPostView restorePost(
            @PathVariable String id,
            @RequestBody(required = false) DeletePostRequest req) {
        return admin.restorePost(id, req == null ? null : req.reason());
    }

    /* ---------- Reports ---------- */

    @GetMapping("/reports")
    public PageResponse<ReportView> listReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportTargetType targetType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return admin.listReports(status, targetType, page, size);
    }

    @PostMapping("/reports/{id}/resolve")
    public ReportView resolveReport(
            @PathVariable String id,
            @Valid @RequestBody ResolveReportRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        return admin.resolveReport(id, req.status(), req.note(), jwt);
    }

    /* ---------- Devotions ---------- */

    @GetMapping("/devotions")
    public PageResponse<AdminDevotionView> listDevotions(
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) Boolean hidden,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return admin.listDevotions(published, hidden, featured, search, page, size);
    }

    @PostMapping("/devotions")
    public AdminDevotionView createDevotion(
            @Valid @RequestBody CreateDevotionRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        return admin.createDevotion(req, jwt);
    }

    @PutMapping("/devotions/{id}")
    public AdminDevotionView updateDevotion(
            @PathVariable String id,
            @Valid @RequestBody UpdateDevotionRequest req) {
        return admin.updateDevotion(id, req);
    }

    @DeleteMapping("/devotions/{id}")
    public void deleteDevotion(
            @PathVariable String id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal Jwt jwt) {
        admin.deleteDevotion(id, reason, jwt);
    }
    /* ---------- Trivia ---------- */

    @GetMapping("/trivia")
    public PageResponse<AdminTriviaView> listTrivia(
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Boolean hidden,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return admin.listTrivia(difficulty, hidden, search, page, size);
    }

    @PostMapping("/trivia")
    public AdminTriviaView createTrivia(
            @Valid @RequestBody CreateTriviaRequest req) {
        return admin.createTrivia(req);
    }

    @PutMapping("/trivia/{id}")
    public AdminTriviaView updateTrivia(
            @PathVariable String id,
            @Valid @RequestBody UpdateTriviaRequest req) {
        return admin.updateTrivia(id, req);
    }

    @DeleteMapping("/trivia/{id}")
    public void deleteTrivia(
            @PathVariable String id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal Jwt jwt) {
        admin.deleteTrivia(id, reason, jwt);
    }
}