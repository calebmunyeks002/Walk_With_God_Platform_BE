package org.walkwithgod.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.walkwithgod.auth.AuthDtos;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository users;

    public UserController(UserRepository u) {
        users = u;
    }

    /** Only `bio` is user-editable. Name and email are locked. */
    public record Update(@Size(max = 1000) String bio) {
    }

    @PutMapping("/me")
    public AuthDtos.UserView update(
            @Valid @RequestBody Update r,
            @AuthenticationPrincipal Jwt jwt) {
        AppUser u = users.findById(UUID.fromString(jwt.getSubject())).orElseThrow();
        u.setBio(r.bio());
        users.save(u);
        return new AuthDtos.UserView(
                u.getId().toString(),
                u.getName(),
                u.getEmail(),
                u.getRole(),
                u.getAvatarUrl(),
                u.getBio(),
                u.getCreatedAt().toString());
    }
}