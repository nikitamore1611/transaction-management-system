package com.transaction.management.repository;

import com.transaction.management.entity.Transaction;
import com.transaction.management.entity.TransactionType;
import com.transaction.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Get all transactions - Oldest to Newest
    List<Transaction> findByUserOrderByTransactionDateAscIdAsc(User user);

    // Check for duplicate transaction
    Optional<Transaction> findFirstByUserAndTransactionDateAndTypeAndAmountAndReason(
            User user,
            LocalDate transactionDate,
            TransactionType type,
            BigDecimal amount,
            String reason
    );

    // Find a specific transaction belonging to a specific user
    Optional<Transaction> findByIdAndUser(Long id, User user);

    // Search transactions by reason/description
    List<Transaction> findByUserAndReasonContainingIgnoreCaseOrderByTransactionDateAscIdAsc(
            User user,
            String reason
    );

    // Filter by Credit or Debit
    List<Transaction> findByUserAndTypeOrderByTransactionDateAscIdAsc(
            User user,
            TransactionType type
    );

    // Filter by date range
    List<Transaction> findByUserAndTransactionDateBetweenOrderByTransactionDateAscIdAsc(
            User user,
            LocalDate startDate,
            LocalDate endDate
    );

    // Filter by amount range
    List<Transaction> findByUserAndAmountBetweenOrderByTransactionDateAscIdAsc(
            User user,
            BigDecimal minAmount,
            BigDecimal maxAmount
    );

    // Sort - Newest to Oldest
    List<Transaction> findByUserOrderByTransactionDateDescIdDesc(
            User user
    );
    void deleteByUser(User user);
}