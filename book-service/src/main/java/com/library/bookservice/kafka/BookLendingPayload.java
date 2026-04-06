package com.library.bookservice.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookLendingPayload(Long userId, Long lendingId, Long copyId, String barcode) {
}
