package com.transaction.management.controller;

import com.transaction.management.dto.TransactionRequest;
import com.transaction.management.entity.Transaction;
import com.transaction.management.entity.TransactionType;
import com.transaction.management.entity.User;
import com.transaction.management.repository.TransactionRepository;
import com.transaction.management.repository.UserRepository;
import com.transaction.management.service.TransactionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionController(
            TransactionService transactionService,
            TransactionRepository transactionRepository,
            UserRepository userRepository) {

        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    // ==============================
    // Get Logged-In User
    // ==============================

    private User getLoggedInUser(Authentication authentication) {

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // ==============================
    // Add Transaction
    // ==============================

    @PostMapping
    public Transaction addTransaction(
            @RequestBody TransactionRequest request,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        return transactionService.addTransaction(
                request,
                user.getId()
        );
    }

    // ==============================
    // Get All Transactions
    // ==============================

    @GetMapping
    public List<Transaction> getTransactions(
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        return transactionRepository
                .findByUserOrderByTransactionDateAscIdAsc(user);
    }

    // ==============================
    // Update Transaction
    // ==============================

    @PutMapping("/{transactionId}")
    public Transaction updateTransaction(
            @PathVariable Long transactionId,
            @RequestBody TransactionRequest request,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        return transactionService.updateTransaction(
                transactionId,
                request,
                user.getId()
        );
    }

    // ==============================
    // Delete Transaction
    // ==============================

    @DeleteMapping("/{transactionId}")
    public String deleteTransaction(
            @PathVariable Long transactionId,
            @RequestParam(defaultValue = "false") boolean confirmDelete,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        transactionService.deleteTransaction(
                transactionId,
                user.getId(),
                confirmDelete
        );

        return "Transaction deleted successfully";
    }

    // ==============================
    // Search Transactions
    // ==============================

    @GetMapping("/search")
    public List<Transaction> searchTransactions(
            @RequestParam String reason,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        return transactionRepository
                .findByUserAndReasonContainingIgnoreCaseOrderByTransactionDateAscIdAsc(
                        user,
                        reason
                );
    }

    // ==============================
    // Filter By Type
    // ==============================

    @GetMapping("/filter/type")
    public List<Transaction> filterByType(
            @RequestParam TransactionType type,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        return transactionRepository
                .findByUserAndTypeOrderByTransactionDateAscIdAsc(
                        user,
                        type
                );
    }

    // ==============================
    // Filter By Date
    // ==============================

    @GetMapping("/filter/date")
    public List<Transaction> filterByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        if (startDate.isAfter(endDate)) {
            throw new RuntimeException(
                    "Start date cannot be after end date"
            );
        }

        return transactionRepository
                .findByUserAndTransactionDateBetweenOrderByTransactionDateAscIdAsc(
                        user,
                        startDate,
                        endDate
                );
    }

    // ==============================
    // Filter By Amount
    // ==============================

    @GetMapping("/filter/amount")
    public List<Transaction> filterByAmountRange(
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        if (minAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(
                    "Minimum amount cannot be negative"
            );
        }

        if (maxAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(
                    "Maximum amount cannot be negative"
            );
        }

        if (minAmount.compareTo(maxAmount) > 0) {
            throw new RuntimeException(
                    "Minimum amount cannot be greater than maximum amount"
            );
        }

        return transactionRepository
                .findByUserAndAmountBetweenOrderByTransactionDateAscIdAsc(
                        user,
                        minAmount,
                        maxAmount
                );
    }

    // ==============================
    // Sort Transactions
    // ==============================

    @GetMapping("/sort")
    public List<Transaction> sortTransactions(
            @RequestParam String order,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        if (order.equalsIgnoreCase("NEWEST")) {

            return transactionRepository
                    .findByUserOrderByTransactionDateDescIdDesc(user);

        } else if (order.equalsIgnoreCase("OLDEST")) {

            return transactionRepository
                    .findByUserOrderByTransactionDateAscIdAsc(user);

        } else {

            throw new RuntimeException(
                    "Invalid order. Use NEWEST or OLDEST"
            );
        }
    }
}