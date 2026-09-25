package org.walkwithgod.auth;

import org.walkwithgod.notification.NotificationService;
import org.walkwithgod.user.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtEncoder jwt;
    private final long expiryMinutes;
    private final NotificationService notifications;

    public AuthService(
            UserRepository users,
            PasswordEncoder encoder,
            JwtEncoder jwt,
            @Value("${app.jwt.expiration-minutes}") long expiryMinutes,
            NotificationService notifications) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.expiryMinutes = expiryMinutes;
        this.notifications = notifications;
    }

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest r) {
        if (users.findByEmailIgnoreCase(r.email()).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        AppUser u = new AppUser();
        u.setName(r.name());
        u.setEmail(r.email());
        u.setPasswordHash(encoder.encode(r.password()));
        u.setRole(Role.MEMBER);
        users.save(u);

        // Welcome email + in-app notification
        notifications.welcome(u);

        return issue(u);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest r) {
        AppUser u = users.findByEmailIgnoreCase(r.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        if (!u.isEnabled() || !encoder.matches(r.password(), u.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        return issue(u);
    }

    public AuthDtos.UserView view(AppUser u) {
        return new AuthDtos.UserView(
                u.getId().toString(),
                u.getName(),
                u.getEmail(),
                u.getRole(),
                u.getAvatarUrl(),
                u.getBio(),
                u.getCreatedAt().toString());
    }

    private AuthDtos.AuthResponse issue(AppUser u) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(u.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(expiryMinutes)))
                .claim("role", u.getRole().name())
                .claim("email", u.getEmail())
                .claim("name", u.getName()) // ← ADDED
                .build();

        String token = jwt.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        claims))
                .getTokenValue();

        return new AuthDtos.AuthResponse(token, view(u));
    }
}