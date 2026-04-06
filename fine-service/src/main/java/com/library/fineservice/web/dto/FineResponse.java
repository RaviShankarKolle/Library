package com.library.fineservice.web.dto;

import com.library.fineservice.domain.Fine;

import java.math.BigDecimal;

public record FineResponse(
        long id,
        long lendingId,
        long userId,
        BigDecimal amount,
        String status,
        String reason,
        String accrualDate,
        String paidAt) {

    public static FineResponse from(Fine f) {
        return new FineResponse(
                f.getId(),
                f.getLendingId(),
                f.getUserId(),
                f.getAmount(),
                f.getStatus(),
                f.getReason(),
                f.getAccrualDate() != null ? f.getAccrualDate().toString() : null,
                f.getPaidAt() != null ? f.getPaidAt().toString() : null);
    }
}
