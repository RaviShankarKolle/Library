package com.library.notificationservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class UserLookupClient {

    private final RestClient userServiceClient;

    public UserLookupClient(RestClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    public Optional<UserInfo> findById(long userId) {
        try {
            UserInfo user = userServiceClient.get().uri("/api/v1/users/{id}", userId).retrieve().body(UserInfo.class);
            return Optional.ofNullable(user);
        } catch (RestClientException ex) {
            return Optional.empty();
        }
    }
}
