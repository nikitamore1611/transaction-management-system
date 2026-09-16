package com.transaction.management.service;

import com.transaction.management.entity.AccountRecoveryOtp;
import com.transaction.management.entity.User;
import com.transaction.management.repository.AccountRecoveryOtpRepository;
import com.transaction.management.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class AccountRecoveryOtpService {

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int RESEND_WAIT_SECONDS = 60;
    private static final int MAX_VERIFICATION_ATTEMPTS = 3;

    private final AccountRecoveryOtpRepository otpRepository;
    private final UserRepository userRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public AccountRecoveryOtpService(
            AccountRecoveryOtpRepository otpRepository,
            UserRepository userRepository) {

        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
    }

    public String generateOtpForInactiveUser(String mobile) {

        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No account found with this mobile number"
                        ));

        if (user.isActive()) {
            throw new RuntimeException(
                    "This account is already active"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        AccountRecoveryOtp previousOtp =
                otpRepository
                        .findTopByUserOrderByCreatedAtDesc(user)
                        .orElse(null);

        if (previousOtp != null &&
                previousOtp.getResendAllowedAt().isAfter(now)) {

            throw new RuntimeException(
                    "Please wait before requesting another OTP"
            );
        }

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        AccountRecoveryOtp recoveryOtp =
                new AccountRecoveryOtp();

        recoveryOtp.setUser(user);
        recoveryOtp.setOtp(otp);
        recoveryOtp.setCreatedAt(now);
        recoveryOtp.setExpiresAt(
                now.plusMinutes(OTP_EXPIRY_MINUTES)
        );
        recoveryOtp.setResendAllowedAt(
                now.plusSeconds(RESEND_WAIT_SECONDS)
        );
        recoveryOtp.setVerificationAttempts(0);
        recoveryOtp.setUsed(false);

        otpRepository.save(recoveryOtp);

        // Temporary development behavior:
        // return the OTP directly.
        // Real SMS integration will be added later.
        return otp;
    }

    public String verifyOtp(
            String mobile,
            String otp) {

        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No account found with this mobile number"
                        ));

        AccountRecoveryOtp recoveryOtp =
                otpRepository
                        .findTopByUserOrderByCreatedAtDesc(user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No OTP has been generated"
                                ));

        if (recoveryOtp.isUsed()) {
            throw new RuntimeException(
                    "This OTP has already been used"
            );
        }

        if (recoveryOtp.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "OTP has expired"
            );
        }

        if (recoveryOtp.getVerificationAttempts()
                >= MAX_VERIFICATION_ATTEMPTS) {

            throw new RuntimeException(
                    "Maximum OTP verification attempts exceeded"
            );
        }

        if (!recoveryOtp.getOtp().equals(otp)) {

            recoveryOtp.setVerificationAttempts(
                    recoveryOtp.getVerificationAttempts() + 1
            );

            otpRepository.save(recoveryOtp);

            throw new RuntimeException(
                    "Invalid OTP"
            );
        }

        recoveryOtp.setUsed(true);
        otpRepository.save(recoveryOtp);

        user.setActive(true);
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);

        userRepository.save(user);

        return "Account reactivated successfully";
    }
}