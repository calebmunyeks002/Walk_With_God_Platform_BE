package org.walkwithgod.audit;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditLogRepository repo;

    public AuditController(AuditLogRepository repo) {
        this.repo = repo;
    }

    public record AuditView(
            String id,
            String createdAt,
            String actorId,
            String actorName,
            String actorRole,
            String action,
            String targetType,
            String targetId,
            String reason,
            String ipAddress,
            boolean success) {
    }

    public record PageResponse<T>(
            List<T> content,
            long totalElements,
            int totalPages,
            int number,
            int size,
            boolean first,
            boolean last) {
    }

    @GetMapping
    public PageResponse<AuditView> list(
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) Boolean success,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (actorId != null) {
                predicates.add(cb.equal(root.get("actorId"), actorId));
            }
            if (action != null) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            if (success != null) {
                predicates.add(cb.equal(root.get("success"), success));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuditLog> result = repo.findAll(
                spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return new PageResponse<>(
                result.getContent().stream().map(a -> new AuditView(
                        a.getId().toString(),
                        a.getCreatedAt().toString(),
                        a.getActorId() == null ? null : a.getActorId().toString(),
                        a.getActorName(),
                        a.getActorRole(),
                        a.getAction().name(),
                        a.getTargetType(),
                        a.getTargetId() == null ? null : a.getTargetId().toString(),
                        a.getReason(),
                        a.getIpAddress(),
                        a.isSuccess())).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize(),
                result.isFirst(),
                result.isLast());
    }
}