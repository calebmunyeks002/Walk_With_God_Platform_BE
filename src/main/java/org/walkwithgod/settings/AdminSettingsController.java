package org.walkwithgod.settings;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.walkwithgod.audit.AuditAction;
import org.walkwithgod.audit.AuditService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/settings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSettingsController {

    private final PlatformSettingRepository repo;
    private final AuditService audit;

    public AdminSettingsController(PlatformSettingRepository repo, AuditService audit) {
        this.repo = repo;
        this.audit = audit;
    }

    public record SettingView(
            String id,
            String key,
            String value,
            String valueType,
            String category,
            String description,
            String updatedAt) {
    }

    public record UpdateRequest(@NotBlank String value) {
    }

    @GetMapping
    public List<SettingView> list() {
        return repo.findAllByOrderByCategoryAscKeyAsc().stream()
                .map(s -> new SettingView(
                        s.getId().toString(),
                        s.getKey(),
                        s.getValue(),
                        s.getValueType(),
                        s.getCategory(),
                        s.getDescription(),
                        s.getUpdatedAt() == null ? null : s.getUpdatedAt().toString()))
                .toList();
    }

    @GetMapping("/grouped")
    public Map<String, List<SettingView>> grouped() {
        var all = repo.findAllByOrderByCategoryAscKeyAsc();
        return all.stream()
                .map(s -> new SettingView(
                        s.getId().toString(),
                        s.getKey(),
                        s.getValue(),
                        s.getValueType(),
                        s.getCategory(),
                        s.getDescription(),
                        s.getUpdatedAt() == null ? null : s.getUpdatedAt().toString()))
                .collect(java.util.stream.Collectors.groupingBy(SettingView::category));
    }

    @Transactional
    @PutMapping("/{key}")
    public SettingView update(
            @PathVariable String key,
            @Valid @RequestBody UpdateRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        PlatformSetting s = repo.findByKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Unknown setting: " + key));

        String oldValue = s.getValue();
        s.setValue(req.value());
        s.setUpdatedAt(Instant.now());
        s.setUpdatedBy(UUID.fromString(jwt.getSubject()));
        repo.save(s);

        audit.record(
                AuditAction.SETTING_UPDATED,
                "SETTING",
                s.getId(),
                "Updated " + key + " from '" + safe(oldValue) + "' to '" + safe(req.value()) + "'");

        return new SettingView(
                s.getId().toString(),
                s.getKey(),
                s.getValue(),
                s.getValueType(),
                s.getCategory(),
                s.getDescription(),
                s.getUpdatedAt().toString());
    }

    @Transactional
    @PutMapping("/bulk")
    public List<SettingView> bulkUpdate(
            @RequestBody Map<String, String> updates,
            @AuthenticationPrincipal Jwt jwt) {
        for (var entry : updates.entrySet()) {
            repo.findByKey(entry.getKey()).ifPresent(s -> {
                s.setValue(entry.getValue());
                s.setUpdatedAt(Instant.now());
                s.setUpdatedBy(UUID.fromString(jwt.getSubject()));
                repo.save(s);
            });
        }
        audit.record(
                AuditAction.SETTING_UPDATED,
                "SETTING",
                null,
                "Bulk update: " + updates.size() + " settings");
        return list();
    }

    private static String safe(String s) {
        if (s == null)
            return "";
        return s.length() <= 60 ? s : s.substring(0, 60) + "…";
    }
}