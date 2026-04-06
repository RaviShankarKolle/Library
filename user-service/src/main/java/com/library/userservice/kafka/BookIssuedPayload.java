package com.library.userservice.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookIssuedPayload(Long userId, Long lendingId, Long copyId, String barcode) {
}
