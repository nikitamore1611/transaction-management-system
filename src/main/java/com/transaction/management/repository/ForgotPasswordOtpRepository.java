package com.transaction.management.repository;

import com.transaction.management.entity.ForgotPasswordOtp;
import com.transaction.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ForgotPasswordOtpRepository
        extends JpaRepository<ForgotPasswordOtp, Long> {

    Optional<ForgotPasswordOtp>
    findTopByUserOrderByCreatedAtDesc(User user);
    void deleteByUser(User user);
}