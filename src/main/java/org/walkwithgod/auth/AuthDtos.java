package org.walkwithgod.auth; import jakarta.validation.constraints.*;import org.walkwithgod.user.*;
public final class AuthDtos { private AuthDtos(){}
 public record RegisterRequest(@NotBlank @Size(max=120) String name,@NotBlank @Email String email,@NotBlank @Size(min=8,max=120) String password){}
 public record LoginRequest(@NotBlank @Email String email,@NotBlank String password){}
 public record UserView(String id,String name,String email,Role role,String avatarUrl,String bio,String createdAt){}
 public record AuthResponse(String token,UserView user){}
}
