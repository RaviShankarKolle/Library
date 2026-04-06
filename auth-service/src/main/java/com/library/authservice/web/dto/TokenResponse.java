package com.library.authservice.web.dto;

public record TokenResponse(String accessToken, String tokenType, int expiresIn) {

    public static TokenResponse of(String accessToken, int expiresIn) {
        return new TokenResponse(accessToken, "Bearer", expiresIn);
    }
}
