package com.library.borrowservice.web.dto;

import com.library.borrowservice.domain.BorrowRecord;

public record BorrowResponse(
        long id,
        long userId,
        long copyId,
        String barcode,
        String status,
        int loanDays,
        String borrowDate,
        String dueDate,
        String returnedAt) {

    public static BorrowResponse from(BorrowRecord r) {
        return new BorrowResponse(
                r.getId(),
                r.getUserId(),
                r.getCopyId(),
                r.getBarcode(),
                r.getStatus(),
                r.getLoanDays(),
                r.getBorrowDate() != null ? r.getBorrowDate().toString() : null,
                r.getDueDate() != null ? r.getDueDate().toString() : null,
                r.getReturnedAt() != null ? r.getReturnedAt().toString() : null);
    }
}
