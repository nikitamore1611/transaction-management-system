package com.transaction.management.service;

import com.transaction.management.entity.User;
import com.transaction.management.repository.AccountRecoveryOtpRepository;
import com.transaction.management.repository.ForgotPasswordOtpRepository;
import com.transaction.management.repository.PasswordHistoryRepository;
import com.transaction.management.repository.TransactionRepository;
import com.transaction.management.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountDeletionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRecoveryOtpRepository accountRecoveryOtpRepository;
    private final ForgotPasswordOtpRepository forgotPasswordOtpRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;

    public AccountDeletionService(
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            AccountRecoveryOtpRepository accountRecoveryOtpRepository,
            ForgotPasswordOtpRepository forgotPasswordOtpRepository,
            PasswordHistoryRepository passwordHistoryRepository) {

        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.accountRecoveryOtpRepository = accountRecoveryOtpRepository;
        this.forgotPasswordOtpRepository = forgotPasswordOtpRepository;
        this.passwordHistoryRepository = passwordHistoryRepository;
    }

    @Transactional
    public void deleteAccount(User user) {

        // Delete financial transactions
        transactionRepository.deleteByUser(user);

        // Delete account recovery OTP records
        accountRecoveryOtpRepository.deleteByUser(user);

        // Delete forgot-password OTP records
        forgotPasswordOtpRepository.deleteByUser(user);

        // Delete password history
        passwordHistoryRepository.deleteByUser(user);

        // Finally delete the user account
        userRepository.delete(user);
    }
}