package com.library.notificationservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserInfo(long id, String email, String role, int maxLimit, int currentCount, boolean blocked) {
}
