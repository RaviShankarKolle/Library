package com.library.userservice.web.dto;

import com.library.userservice.domain.User;

public record UserResponse(
        long id,
        String email,
        String role,
        int maxLimit,
        int currentCount,
        boolean blocked) {

    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getRole(),
                u.getMaxLimit(),
                u.getCurrentCount(),
                u.isBlocked());
    }
}
