package com.transaction.management.service;

import com.transaction.management.dto.DashboardRequest;
import com.transaction.management.dto.DashboardResponse;
import com.transaction.management.dto.MonthlySummary;
import com.transaction.management.entity.Transaction;
import com.transaction.management.entity.TransactionType;
import com.transaction.management.entity.User;
import com.transaction.management.repository.TransactionRepository;
import com.transaction.management.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {

        private final TransactionRepository transactionRepository;
        private final UserRepository userRepository;

        public DashboardService(
                        TransactionRepository transactionRepository,
                        UserRepository userRepository) {

                this.transactionRepository = transactionRepository;
                this.userRepository = userRepository;
        }

        public DashboardResponse getDashboard(
                        Long userId,
                        DashboardRequest request) {

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                if (request == null || request.getPeriod() == null) {
                        throw new RuntimeException(
                                        "Dashboard period is required");
                }

                String period = request.getPeriod()
                                .trim()
                                .toUpperCase();

                LocalDate startDate;
                LocalDate endDate;

                switch (period) {

                        case "CURRENT_MONTH":

                                startDate = LocalDate.now()
                                                .withDayOfMonth(1);

                                endDate = startDate
                                                .withDayOfMonth(
                                                                startDate.lengthOfMonth());

                                break;

                        case "PREVIOUS_MONTH":

                                LocalDate previousMonth = LocalDate.now().minusMonths(1);

                                startDate = previousMonth
                                                .withDayOfMonth(1);

                                endDate = startDate
                                                .withDayOfMonth(
                                                                startDate.lengthOfMonth());

                                break;

                        case "CUSTOM":

                                startDate = request.getStartDate();
                                endDate = request.getEndDate();

                                if (startDate == null || endDate == null) {
                                        throw new RuntimeException(
                                                        "Start date and end date are required for CUSTOM period");
                                }

                                if (startDate.isAfter(endDate)) {
                                        throw new RuntimeException(
                                                        "Start date cannot be after end date");
                                }

                                break;

                        default:

                                throw new RuntimeException(
                                                "Invalid dashboard period. Use CURRENT_MONTH, PREVIOUS_MONTH or CUSTOM");
                }

                /*
                 * Get transactions within the selected period.
                 */
                List<Transaction> periodTransactions = transactionRepository
                                .findByUserAndTransactionDateBetweenOrderByTransactionDateAscIdAsc(
                                                user,
                                                startDate,
                                                endDate);

                /*
                 * Apply search and filters.
                 */
                List<Transaction> filteredTransactions = applyFilters(
                                periodTransactions,
                                request);

                /*
                 * Apply sorting.
                 */
                applySorting(
                                filteredTransactions,
                                request.getSortOrder());

                /*
                 * Calculate totals after applying
                 * search and filters.
                 */
                BigDecimal totalCredit = BigDecimal.ZERO;
                BigDecimal totalDebit = BigDecimal.ZERO;

                for (Transaction transaction : filteredTransactions) {

                        if (transaction.getType() == TransactionType.CREDIT) {

                                totalCredit = totalCredit.add(
                                                transaction.getAmount());

                        } else if (transaction.getType() == TransactionType.DEBIT) {

                                totalDebit = totalDebit.add(
                                                transaction.getAmount());
                        }
                }

                /*
                 * Current balance is always the actual
                 * balance as of today.
                 *
                 * It is not affected by dashboard
                 * filters or sorting.
                 */
                List<Transaction> allTransactions = transactionRepository
                                .findByUserOrderByTransactionDateAscIdAsc(user);

                BigDecimal currentBalance = calculateCurrentBalance(
                                user,
                                allTransactions);

                /*
                 * Get the 10 most recent transactions
                 * according to the selected sorting order.
                 *
                 * If no sort order is supplied,
                 * default to NEWEST.
                 */
                List<Transaction> recentTransactions = new ArrayList<>(filteredTransactions);
                if (request.isShowAll()) {
                        // Keep all filtered transactions
                } else if (recentTransactions.size() > 10) {

                        recentTransactions = new ArrayList<>(
                                        recentTransactions.subList(0, 10));
                }

                if (request.getSortOrder() == null
                                || request.getSortOrder().trim().isEmpty()) {

                        applySorting(
                                        recentTransactions,
                                        "NEWEST");
                }

                /*
                 * Monthly summary is based on the selected
                 * date period, not on search/filter values.
                 */
                List<MonthlySummary> monthlySummary = generateMonthlySummary(
                                user,
                                allTransactions,
                                startDate,
                                endDate);

                DashboardResponse response = new DashboardResponse();

                response.setPeriod(period);
                response.setStartDate(startDate);
                response.setEndDate(endDate);
                response.setCurrentBalance(currentBalance);
                response.setTotalCredit(totalCredit);
                response.setTotalDebit(totalDebit);
                response.setRecentTransactions(recentTransactions);
                response.setMonthlySummary(monthlySummary);

                return response;
        }

        private List<Transaction> applyFilters(
                        List<Transaction> transactions,
                        DashboardRequest request) {

                List<Transaction> filteredTransactions = new ArrayList<>(transactions);

                /*
                 * Search by Reason / Description.
                 */
                if (request.getSearchReason() != null
                                && !request.getSearchReason()
                                                .trim()
                                                .isEmpty()) {

                        String searchText = request.getSearchReason()
                                        .trim()
                                        .toLowerCase();

                        filteredTransactions.removeIf(
                                        transaction -> transaction.getReason() == null
                                                        ||
                                                        !transaction.getReason()
                                                                        .toLowerCase()
                                                                        .contains(searchText));
                }

                /*
                 * Filter by Credit or Debit.
                 */
                if (request.getType() != null) {

                        filteredTransactions.removeIf(
                                        transaction -> transaction.getType() != request.getType());
                }

                /*
                 * Filter by minimum amount.
                 */
                if (request.getMinAmount() != null) {

                        if (request.getMinAmount()
                                        .compareTo(BigDecimal.ZERO) < 0) {

                                throw new RuntimeException(
                                                "Minimum amount cannot be negative");
                        }

                        filteredTransactions.removeIf(
                                        transaction -> transaction.getAmount()
                                                        .compareTo(
                                                                        request.getMinAmount()) < 0);
                }

                /*
                 * Filter by maximum amount.
                 */
                if (request.getMaxAmount() != null) {

                        if (request.getMaxAmount()
                                        .compareTo(BigDecimal.ZERO) < 0) {

                                throw new RuntimeException(
                                                "Maximum amount cannot be negative");
                        }

                        filteredTransactions.removeIf(
                                        transaction -> transaction.getAmount()
                                                        .compareTo(
                                                                        request.getMaxAmount()) > 0);
                }

                /*
                 * Validate amount range.
                 */
                if (request.getMinAmount() != null
                                && request.getMaxAmount() != null
                                && request.getMinAmount()
                                                .compareTo(request.getMaxAmount()) > 0) {

                        throw new RuntimeException(
                                        "Minimum amount cannot be greater than maximum amount");
                }

                return filteredTransactions;
        }

        private void applySorting(
                        List<Transaction> transactions,
                        String sortOrder) {

                /*
                 * Default sorting:
                 * Newest to Oldest
                 */
                if (sortOrder == null
                                || sortOrder.trim().isEmpty()) {

                        sortOrder = "NEWEST";
                }

                String order = sortOrder.trim().toUpperCase();

                /*
                 * Newest to Oldest
                 */
                if (order.equals("NEWEST")) {

                        transactions.sort(
                                        Comparator
                                                        .comparing(
                                                                        Transaction::getTransactionDate)
                                                        .thenComparing(
                                                                        Transaction::getId)
                                                        .reversed());

                }

                /*
                 * Oldest to Newest
                 */
                else if (order.equals("OLDEST")) {

                        transactions.sort(
                                        Comparator
                                                        .comparing(
                                                                        Transaction::getTransactionDate)
                                                        .thenComparing(
                                                                        Transaction::getId));

                }

                /*
                 * Invalid sorting option
                 */
                else {

                        throw new RuntimeException(
                                        "Invalid sort order. Use NEWEST or OLDEST");
                }
        }

        private List<MonthlySummary> generateMonthlySummary(
                        User user,
                        List<Transaction> allTransactions,
                        LocalDate startDate,
                        LocalDate endDate) {

                List<MonthlySummary> summaries = new ArrayList<>();

                YearMonth firstMonth = YearMonth.from(startDate);

                YearMonth lastMonth = YearMonth.from(endDate);

                YearMonth currentMonth = firstMonth;

                while (!currentMonth.isAfter(lastMonth)) {

                        LocalDate monthStart = currentMonth.atDay(1);

                        LocalDate monthEnd = currentMonth.atEndOfMonth();

                        BigDecimal totalCredit = BigDecimal.ZERO;

                        BigDecimal totalDebit = BigDecimal.ZERO;

                        BigDecimal closingBalance = user.getOpeningBalance();

                        /*
                         * Calculate balance up to the end
                         * of this month.
                         */
                        for (Transaction transaction : allTransactions) {

                                if (!transaction.getTransactionDate()
                                                .isAfter(monthEnd)) {

                                        if (transaction.getType() == TransactionType.CREDIT) {

                                                closingBalance = closingBalance.add(
                                                                transaction.getAmount());

                                        } else if (transaction.getType() == TransactionType.DEBIT) {

                                                closingBalance = closingBalance.subtract(
                                                                transaction.getAmount());
                                        }
                                }

                                /*
                                 * Calculate Credit and Debit
                                 * for this particular month.
                                 */
                                if (!transaction.getTransactionDate()
                                                .isBefore(monthStart)
                                                &&
                                                !transaction.getTransactionDate()
                                                                .isAfter(monthEnd)) {

                                        if (transaction.getType() == TransactionType.CREDIT) {

                                                totalCredit = totalCredit.add(
                                                                transaction.getAmount());

                                        } else if (transaction.getType() == TransactionType.DEBIT) {

                                                totalDebit = totalDebit.add(
                                                                transaction.getAmount());
                                        }
                                }
                        }

                        MonthlySummary summary = new MonthlySummary();

                        summary.setYear(
                                        currentMonth.getYear());

                        summary.setMonth(
                                        currentMonth.getMonthValue());

                        summary.setTotalCredit(
                                        totalCredit);

                        summary.setTotalDebit(
                                        totalDebit);

                        summary.setClosingBalance(
                                        closingBalance);

                        summaries.add(summary);

                        currentMonth = currentMonth.plusMonths(1);
                }

                return summaries;
        }

        private BigDecimal calculateCurrentBalance(
                        User user,
                        List<Transaction> transactions) {

                BigDecimal balance = user.getOpeningBalance();

                /*
                 * Calculate current balance by transaction date.
                 *
                 * Same-day transactions are aggregated together.
                 * The system does not record transaction time,
                 * so transactions on the same date have no assumed order.
                 */
                LocalDate currentDate = null;

                BigDecimal dailyCredit = BigDecimal.ZERO;
                BigDecimal dailyDebit = BigDecimal.ZERO;

                for (Transaction transaction : transactions) {

                        if (currentDate == null) {
                                currentDate = transaction.getTransactionDate();
                        }

                        /*
                         * When the date changes, apply the previous
                         * day's total effect to the balance.
                         */
                        if (!transaction.getTransactionDate()
                                        .isEqual(currentDate)) {

                                balance = balance
                                                .add(dailyCredit)
                                                .subtract(dailyDebit);

                                currentDate = transaction.getTransactionDate();

                                dailyCredit = BigDecimal.ZERO;
                                dailyDebit = BigDecimal.ZERO;
                        }

                        /*
                         * Collect all transactions for the same day.
                         */
                        if (transaction.getType() == TransactionType.CREDIT) {

                                dailyCredit = dailyCredit.add(
                                                transaction.getAmount());

                        } else if (transaction.getType() == TransactionType.DEBIT) {

                                dailyDebit = dailyDebit.add(
                                                transaction.getAmount());
                        }
                }

                /*
                 * Apply the final day's transactions.
                 */
                balance = balance
                                .add(dailyCredit)
                                .subtract(dailyDebit);

                return balance;
        }
}