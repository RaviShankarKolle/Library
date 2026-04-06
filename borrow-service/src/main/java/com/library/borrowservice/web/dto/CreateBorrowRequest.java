package com.library.borrowservice.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBorrowRequest(
        @NotNull Long userId,
        @NotBlank @Size(max = 64) String barcode,
        Integer loanDays) {

    public int resolvedLoanDays() {
        return loanDays == null || loanDays < 1 ? 14 : loanDays;
    }
}
