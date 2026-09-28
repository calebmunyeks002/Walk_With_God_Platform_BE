package org.walkwithgod.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.walkwithgod.audit.AuditAction;
import org.walkwithgod.audit.AuditService;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/me")
public class PasswordController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuditService audit;

    public PasswordController(
            UserRepository users,
            PasswordEncoder encoder,
            AuditService audit) {
        this.users = users;
        this.encoder = encoder;
        this.audit = audit;
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, max = 128) String newPassword) {
    }

    @PostMapping("/change-password")
    @Transactional
    public void change(
            @Valid @RequestBody ChangePasswordRequest r,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        AppUser u = users.findById(uid).orElseThrow();

        if (!encoder.matches(r.currentPassword(), u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect.");
        }
        if (encoder.matches(r.newPassword(), u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different.");
        }

        u.setPasswordHash(encoder.encode(r.newPassword()));
        users.save(u);

        audit.record(AuditAction.USER_UPDATED, "USER", u.getId(),
                "Password changed");
    }
}