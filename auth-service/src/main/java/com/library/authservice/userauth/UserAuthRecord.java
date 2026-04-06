package com.library.authservice.userauth;

public record UserAuthRecord(long id, String email, String passwordHash, String role) {
}
