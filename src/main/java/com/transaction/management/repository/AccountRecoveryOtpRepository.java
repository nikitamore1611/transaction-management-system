package com.transaction.management.repository;

import com.transaction.management.entity.AccountRecoveryOtp;
import com.transaction.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRecoveryOtpRepository
        extends JpaRepository<AccountRecoveryOtp, Long> {

    Optional<AccountRecoveryOtp>
    findTopByUserOrderByCreatedAtDesc(User user);
    void deleteByUser(User user);
}