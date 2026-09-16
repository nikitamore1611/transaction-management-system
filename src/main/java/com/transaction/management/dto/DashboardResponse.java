package com.transaction.management.dto;

import com.transaction.management.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DashboardResponse {

    private String period;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal currentBalance;

    private BigDecimal totalCredit;

    private BigDecimal totalDebit;

    private List<Transaction> recentTransactions;

    private List<MonthlySummary> monthlySummary;

    public DashboardResponse() {
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

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public BigDecimal getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(BigDecimal totalCredit) {
        this.totalCredit = totalCredit;
    }

    public BigDecimal getTotalDebit() {
        return totalDebit;
    }

    public void setTotalDebit(BigDecimal totalDebit) {
        this.totalDebit = totalDebit;
    }

    public List<Transaction> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(
            List<Transaction> recentTransactions) {

        this.recentTransactions = recentTransactions;
    }

    public List<MonthlySummary> getMonthlySummary() {
        return monthlySummary;
    }

    public void setMonthlySummary(
            List<MonthlySummary> monthlySummary) {

        this.monthlySummary = monthlySummary;
    }
}