package com.transaction.management.dto;

import com.transaction.management.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionRequest {

    private LocalDate transactionDate;
    private TransactionType type;
    private BigDecimal amount;
    private String reason;
    private boolean saveAnyway;
    private boolean confirmUpdate;

    public TransactionRequest() {
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isSaveAnyway() {
        return saveAnyway;
    }

    public void setSaveAnyway(boolean saveAnyway) {
        this.saveAnyway = saveAnyway;
    }

    public boolean isConfirmUpdate() {
        return confirmUpdate;
    }

    public void setConfirmUpdate(boolean confirmUpdate) {
        this.confirmUpdate = confirmUpdate;
    }
}