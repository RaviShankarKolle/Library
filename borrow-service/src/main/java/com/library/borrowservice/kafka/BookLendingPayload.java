package com.library.borrowservice.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BookLendingPayload(
        @JsonProperty("userId") Long userId,
        @JsonProperty("lendingId") Long lendingId,
        @JsonProperty("copyId") Long copyId,
        @JsonProperty("barcode") String barcode) {
}
