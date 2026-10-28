package org.walkwithgod.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

@Configuration
public class SupabaseConfig {

    @Value("${app.supabase.jwks-url}")
    private String jwksUrl;

    @Value("${app.supabase.project-ref}")
    private String projectRef;

    @Bean("supabaseJwtDecoder")
    public JwtDecoder supabaseJwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUrl).build();
        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer("https://" + projectRef + ".supabase.co/auth/v1");
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator));
        return decoder;
    }
}