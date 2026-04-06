package com.library.borrowservice.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceClient {

    private final RestClient userServiceClient;

    public UserServiceClient(@Qualifier("userServiceClient") RestClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    public void validateBorrowEligibility(long userId, String authorizationHeader) {
        var req = userServiceClient.post().uri("/api/v1/users/{id}/borrow/validate", userId);
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            req = req.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
        req.retrieve().toBodilessEntity();
    }
}
