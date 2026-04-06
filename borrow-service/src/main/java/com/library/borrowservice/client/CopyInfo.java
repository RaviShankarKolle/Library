package com.library.borrowservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CopyInfo(long id, long bookId, String barcode, String status) {
}
