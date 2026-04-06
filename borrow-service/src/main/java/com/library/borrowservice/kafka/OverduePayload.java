package com.library.borrowservice.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OverduePayload(
        @JsonProperty("userId") Long userId,
        @JsonProperty("lendingId") Long lendingId,
        @JsonProperty("copyId") Long copyId,
        @JsonProperty("barcode") String barcode,
        @JsonProperty("dueDate") String dueDate) {
}
