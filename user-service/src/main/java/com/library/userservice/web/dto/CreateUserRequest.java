package com.library.userservice.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(max = 32) String role,
        @Min(1) int maxLimit,
        @NotBlank @Size(min = 8, max = 72) String password) {
}
