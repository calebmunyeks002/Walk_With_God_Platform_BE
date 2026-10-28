package org.walkwithgod.auth;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.walkwithgod.notification.NotificationService;
import org.walkwithgod.user.AppUser;
import org.walkwithgod.user.Role;
import org.walkwithgod.user.UserRepository;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class OAuthController {

    private final JwtDecoder supabaseDecoder;
    private final UserRepository users;
    private final AuthService auth;
    private final NotificationService notifications;

    public OAuthController(
            @Qualifier("supabaseJwtDecoder") JwtDecoder supabaseDecoder,
            UserRepository users,
            AuthService auth,
            NotificationService notifications) {
        this.supabaseDecoder = supabaseDecoder;
        this.users = users;
        this.auth = auth;
        this.notifications = notifications;
    }

    public record OAuthLoginRequest(String supabaseToken) { }

    @PostMapping("/oauth-login")
    public AuthDtos.AuthResponse oauthLogin(@RequestBody OAuthLoginRequest body) {
        if (body == null || body.supabaseToken() == null || body.supabaseToken().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Supabase token");
        }

        Jwt supaJwt;
        try {
            supaJwt = supabaseDecoder.decode(body.supabaseToken());
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired Supabase token");
        }

        String email = supaJwt.getClaimAsString("email");
        String name = supaJwt.getClaimAsString("user_metadata.full_name");
        String avatar = supaJwt.getClaimAsString("user_metadata.avatar_url");

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OAuth provider did not return an email");
        }

        String finalName = (name != null && !name.isBlank()) ? name : email.split("@")[0];

        AppUser user = users.findByEmailIgnoreCase(email).orElse(null);
        boolean isNew = false;

        if (user == null) {
            user = new AppUser();
            user.setName(finalName);
            user.setEmail(email);
            user.setPasswordHash(UUID.randomUUID().toString());
            user.setRole(Role.MEMBER);
            user.setAvatarUrl(avatar);
            users.save(user);
            isNew = true;
        } else if (avatar != null && !avatar.equals(user.getAvatarUrl())) {
            user.setAvatarUrl(avatar);
            users.save(user);
        }

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account has been disabled");
        }

        if (isNew) {
            notifications.welcome(user);
        }

        return auth.issueFor(user);
    }
}