package org.walkwithgod.auth;
import java.util.UUID;
import jakarta.validation.Valid;
import org.walkwithgod.user.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth") public class AuthController {private final AuthService auth;private final UserRepository users;public AuthController(AuthService a,UserRepository u){auth=a;users=u;}
 @PostMapping("/register") public AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.RegisterRequest r){return auth.register(r);}
 @PostMapping("/login") public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest r){return auth.login(r);}
 @GetMapping("/me") public AuthDtos.UserView me(@AuthenticationPrincipal Jwt jwt){return auth.view(users.findById(UUID.fromString(jwt.getSubject())).orElseThrow());}
}
