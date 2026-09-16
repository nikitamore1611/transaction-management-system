package com.transaction.management.service;

import com.transaction.management.dto.MonthlyReportResponse;
import com.transaction.management.entity.Transaction;
import com.transaction.management.entity.TransactionType;
import com.transaction.management.entity.User;
import com.transaction.management.repository.TransactionRepository;
import com.transaction.management.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class MonthlyReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public MonthlyReportService(
            TransactionRepository transactionRepository,
            UserRepository userRepository) {

        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public MonthlyReportResponse generateMonthlyReport(
            Long userId,
            int year,
            int month) {

        if (month < 1 || month > 12) {
            throw new RuntimeException(
                    "Month must be between 1 and 12"
            );
        }

        if (year < 1) {
            throw new RuntimeException(
                    "Invalid year"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        LocalDate startDate = LocalDate.of(year, month, 1);

        LocalDate endDate = startDate.withDayOfMonth(
                startDate.lengthOfMonth()
        );

        List<Transaction> monthlyTransactions =
                transactionRepository
                        .findByUserAndTransactionDateBetweenOrderByTransactionDateAscIdAsc(
                                user,
                                startDate,
                                endDate
                        );

        BigDecimal openingBalance = calculateOpeningBalance(
                user,
                startDate
        );

        BigDecimal totalCredit = BigDecimal.ZERO;
        BigDecimal totalDebit = BigDecimal.ZERO;

        for (Transaction transaction : monthlyTransactions) {

            if (transaction.getType() == TransactionType.CREDIT) {

                totalCredit = totalCredit.add(
                        transaction.getAmount()
                );

            } else if (transaction.getType() == TransactionType.DEBIT) {

                totalDebit = totalDebit.add(
                        transaction.getAmount()
                );
            }
        }

        BigDecimal closingBalance = openingBalance
                .add(totalCredit)
                .subtract(totalDebit);

        MonthlyReportResponse report =
                new MonthlyReportResponse();

        report.setYear(year);
        report.setMonth(month);
        report.setOpeningBalance(openingBalance);
        report.setTotalCredit(totalCredit);
        report.setTotalDebit(totalDebit);
        report.setClosingBalance(closingBalance);
        report.setTotalTransactions(
                monthlyTransactions.size()
        );
        report.setTransactions(monthlyTransactions);

        if (monthlyTransactions.isEmpty()) {

            report.setMessage(
                    "No transactions recorded for this month."
            );

        } else {

            report.setMessage(
                    "Transactions recorded for this month."
            );
        }

        return report;
    }

    private BigDecimal calculateOpeningBalance(
            User user,
            LocalDate startDate) {

        List<Transaction> previousTransactions =
                transactionRepository
                        .findByUserOrderByTransactionDateAscIdAsc(user);

        BigDecimal balance = user.getOpeningBalance();

        for (Transaction transaction : previousTransactions) {

            if (transaction.getTransactionDate()
                    .isBefore(startDate)) {

                if (transaction.getType()
                        == TransactionType.CREDIT) {

                    balance = balance.add(
                            transaction.getAmount()
                    );

                } else if (transaction.getType()
                        == TransactionType.DEBIT) {

                    balance = balance.subtract(
                            transaction.getAmount()
                    );
                }

            } else {
                break;
            }
        }

        return balance;
    }
}