package com.transaction.management.dto;

import com.transaction.management.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DashboardRequest {

    private String period;

    private LocalDate startDate;

    private LocalDate endDate;

    private String searchReason;

    private TransactionType type;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private String sortOrder;

    private boolean showAll;

    public DashboardRequest() {
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getSearchReason() {
        return searchReason;
    }

    public void setSearchReason(String searchReason) {
        this.searchReason = searchReason;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isShowAll() {
        return showAll;
    }

    public void setShowAll(boolean showAll) {
        this.showAll = showAll;
    }
}