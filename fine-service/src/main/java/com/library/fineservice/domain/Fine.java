package com.library.fineservice.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class Fine {

    private Long id;
    private long lendingId;
    private long userId;
    private BigDecimal amount;
    private String status;
    private String reason;
    private LocalDate accrualDate;
    private Instant paidAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getLendingId() {
        return lendingId;
    }

    public void setLendingId(long lendingId) {
        this.lendingId = lendingId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getAccrualDate() {
        return accrualDate;
    }

    public void setAccrualDate(LocalDate accrualDate) {
        this.accrualDate = accrualDate;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }
}
