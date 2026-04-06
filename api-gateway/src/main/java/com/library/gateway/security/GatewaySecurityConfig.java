package com.library.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    private final ReactiveJwtAuthenticationConverter reactiveJwtAuthenticationConverter;

    public GatewaySecurityConfig(ReactiveJwtAuthenticationConverter reactiveJwtAuthenticationConverter) {
        this.reactiveJwtAuthenticationConverter = reactiveJwtAuthenticationConverter;
    }

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(c -> c.configurationSource(cors()))
                .authorizeExchange(ex -> ex.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/api/v1/auth/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/users").hasRole("LIBRARIAN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/books").hasRole("LIBRARIAN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/books/*/copies").hasRole("LIBRARIAN")
                        .pathMatchers(HttpMethod.GET, "/api/v1/copies/barcode/*").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/v1/copies/barcode/*/reserve")
                        .hasAnyRole("PATRON", "LIBRARIAN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/copies/barcode/*/release-reservation")
                        .hasAnyRole("PATRON", "LIBRARIAN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/users/*/borrow/validate").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/v1/borrows/*/issue").hasRole("LIBRARIAN")
                        .pathMatchers(HttpMethod.GET, "/api/v1/borrows/overdue").hasRole("LIBRARIAN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/borrows").hasAnyRole("PATRON", "LIBRARIAN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/borrows/*/return").authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/v1/users/*/fines").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/v1/fines/*/pay").authenticated()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(reactiveJwtAuthenticationConverter)));
        return http.build();
    }

    private static CorsConfigurationSource cors() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOriginPatterns(List.of("*"));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }

    @Bean
    ReactiveJwtDecoder reactiveJwtDecoder(org.springframework.core.env.Environment env) {
        String b64 = env.getRequiredProperty("app.jwt.secret-base64");
        byte[] secret = Base64.getDecoder().decode(b64);
        if (secret.length < 32) {
            throw new IllegalStateException("app.jwt.secret-base64 must decode to at least 32 bytes for HS256");
        }
        var key = new SecretKeySpec(secret, "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
