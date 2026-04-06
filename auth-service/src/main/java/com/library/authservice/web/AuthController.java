package com.library.authservice.web;

import com.library.authservice.service.LibraryLoginService;
import com.library.authservice.web.dto.LoginRequest;
import com.library.authservice.web.dto.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LibraryLoginService libraryLoginService;

    public AuthController(LibraryLoginService libraryLoginService) {
        this.libraryLoginService = libraryLoginService;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest body) {
        LibraryLoginService.TokenResult r = libraryLoginService.login(body.email(), body.password());
        return TokenResponse.of(r.accessToken(), r.expiresInSeconds());
    }
}
