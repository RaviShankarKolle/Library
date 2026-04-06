package com.library.authservice.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class LibraryJwtService {

    private final SecretKey key;
    private final int validitySeconds;

    public LibraryJwtService(
            @Value("${app.jwt.secret-base64}") String secretBase64,
            @Value("${app.jwt.access-token-validity-seconds}") int validitySeconds) {
        byte[] bytes = Decoders.BASE64.decode(secretBase64);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 256 bits");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.validitySeconds = validitySeconds;
    }

    public String createAccessToken(long userId, String email, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(validitySeconds);
        return Jwts.builder()
                .subject(Long.toString(userId))
                .claim("email", email)
                .claim("roles", List.of(role))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public int getValiditySeconds() {
        return validitySeconds;
    }
}
