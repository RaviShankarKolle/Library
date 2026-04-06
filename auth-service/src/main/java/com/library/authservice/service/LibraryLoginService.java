package com.library.authservice.service;

import com.library.authservice.jwt.LibraryJwtService;
import com.library.authservice.userauth.UserAuthRecord;
import com.library.authservice.userauth.UserAuthRepository;
import com.library.authservice.web.error.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LibraryLoginService {

    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final LibraryJwtService libraryJwtService;

    public LibraryLoginService(
            UserAuthRepository userAuthRepository,
            PasswordEncoder passwordEncoder,
            LibraryJwtService libraryJwtService) {
        this.userAuthRepository = userAuthRepository;
        this.passwordEncoder = passwordEncoder;
        this.libraryJwtService = libraryJwtService;
    }

    public TokenResult login(String email, String rawPassword) {
        UserAuthRecord user =
                userAuthRepository.findByEmail(email).orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (user.passwordHash() == null || user.passwordHash().isBlank()) {
            throw new UnauthorizedException("Account has no password set");
        }
        if (!passwordEncoder.matches(rawPassword, user.passwordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        String token = libraryJwtService.createAccessToken(user.id(), user.email(), user.role());
        return new TokenResult(token, libraryJwtService.getValiditySeconds());
    }

    public record TokenResult(String accessToken, int expiresInSeconds) {}
}
