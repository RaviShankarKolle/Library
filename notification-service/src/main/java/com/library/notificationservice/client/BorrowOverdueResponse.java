package com.library.notificationservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BorrowOverdueResponse(
        long id,
        long userId,
        long copyId,
        String barcode,
        String status,
        int loanDays,
        String borrowDate,
        String dueDate,
        String returnedAt) {
}

