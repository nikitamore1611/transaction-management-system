package com.transaction.management.service;

import com.transaction.management.dto.TransactionRequest;
import com.transaction.management.entity.Transaction;
import com.transaction.management.entity.TransactionType;
import com.transaction.management.entity.User;
import com.transaction.management.repository.TransactionRepository;
import com.transaction.management.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Transaction addTransaction(TransactionRequest request, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateTransactionRequest(request);

        String cleanedReason = request.getReason().trim();

        boolean duplicateExists =
                transactionRepository
                        .findFirstByUserAndTransactionDateAndTypeAndAmountAndReason(
                                user,
                                request.getTransactionDate(),
                                request.getType(),
                                request.getAmount(),
                                cleanedReason
                        )
                        .isPresent();

        if (duplicateExists && !request.isSaveAnyway()) {
            throw new DuplicateTransactionException(
                    "A similar transaction already exists. Do you want to save this transaction anyway?"
            );
        }

        List<Transaction> transactions =
                transactionRepository.findByUserOrderByTransactionDateAscIdAsc(user);

        Transaction newTransaction = new Transaction();

        newTransaction.setTransactionDate(request.getTransactionDate());
        newTransaction.setType(request.getType());
        newTransaction.setAmount(request.getAmount());
        newTransaction.setReason(cleanedReason);
        newTransaction.setUser(user);

        transactions = new ArrayList<>(transactions);
        transactions.add(newTransaction);

        transactions.sort(
                Comparator
                        .comparing(Transaction::getTransactionDate)
                        .thenComparing(
                                Transaction::getId,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
        );

        calculateBalances(user, transactions);

        Transaction savedTransaction =
                transactionRepository.save(newTransaction);

        for (Transaction currentTransaction : transactions) {

            if (currentTransaction.getId() != null) {
                transactionRepository.save(currentTransaction);
            }
        }

        return savedTransaction;
    }

    @Transactional
    public Transaction updateTransaction(
            Long transactionId,
            TransactionRequest request,
            Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateTransactionRequest(request);

        Transaction transaction =
                transactionRepository.findByIdAndUser(transactionId, user)
                        .orElseThrow(() ->
                                new RuntimeException("Transaction not found"));

        if (!request.isConfirmUpdate()) {
            throw new UpdateConfirmationException(
                    "Are you sure you want to update this transaction?"
            );
        }

        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setReason(request.getReason().trim());

        List<Transaction> transactions =
                transactionRepository.findByUserOrderByTransactionDateAscIdAsc(user);

        transactions = new ArrayList<>(transactions);

        transactions.sort(
                Comparator
                        .comparing(Transaction::getTransactionDate)
                        .thenComparing(
                                Transaction::getId,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
        );

        calculateBalances(user, transactions);

        transactionRepository.save(transaction);

        for (Transaction currentTransaction : transactions) {
            transactionRepository.save(currentTransaction);
        }

        return transaction;
    }

    @Transactional
    public void deleteTransaction(
            Long transactionId,
            Long userId,
            boolean confirmDelete) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction transaction =
                transactionRepository.findByIdAndUser(transactionId, user)
                        .orElseThrow(() ->
                                new RuntimeException("Transaction not found"));

        if (!confirmDelete) {
            throw new DeleteConfirmationException(
                    "Are you sure you want to delete this transaction?"
            );
        }

        transactionRepository.delete(transaction);

        List<Transaction> remainingTransactions =
                transactionRepository
                        .findByUserOrderByTransactionDateAscIdAsc(user);

        remainingTransactions = new ArrayList<>(remainingTransactions);

        remainingTransactions.sort(
                Comparator
                        .comparing(Transaction::getTransactionDate)
                        .thenComparing(
                                Transaction::getId,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
        );

        calculateBalances(user, remainingTransactions);

        for (Transaction currentTransaction : remainingTransactions) {
            transactionRepository.save(currentTransaction);
        }
    }

    private void validateTransactionRequest(TransactionRequest request) {

        if (request.getAmount() == null ||
                request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Transaction amount must be greater than ₹0"
            );
        }

        if (request.getAmount().scale() > 2) {
            throw new RuntimeException(
                    "Transaction amount cannot have more than 2 decimal places"
            );
        }

        if (request.getTransactionDate() == null) {
            throw new RuntimeException(
                    "Transaction date is required"
            );
        }

        if (request.getTransactionDate().isAfter(LocalDate.now())) {
            throw new RuntimeException(
                    "Future-dated transactions are not allowed"
            );
        }

        if (request.getType() == null) {
            throw new RuntimeException(
                    "Transaction type is required. Select CREDIT or DEBIT"
            );
        }

        if (request.getReason() == null ||
                request.getReason().trim().isEmpty()) {

            throw new RuntimeException(
                    "Transaction reason is required"
            );
        }

        if (request.getReason().length() > 100) {
            throw new RuntimeException(
                    "Transaction reason cannot exceed 100 characters"
            );
        }
    }

    private void calculateBalances(
            User user,
            List<Transaction> transactions) {

        BigDecimal balance = user.getOpeningBalance();

        for (Transaction currentTransaction : transactions) {

            if (currentTransaction.getType() == TransactionType.CREDIT) {
                balance = balance.add(currentTransaction.getAmount());

            } else if (currentTransaction.getType() == TransactionType.DEBIT) {
                balance = balance.subtract(currentTransaction.getAmount());
            }

            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException(
                        "Transaction cannot be saved because balance cannot be negative"
                );
            }

            currentTransaction.setBalance(balance);
        }
    }
}