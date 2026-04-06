package com.library.fineservice.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OverduePayload(Long userId, Long lendingId, Long copyId, String barcode, String dueDate) {
}
