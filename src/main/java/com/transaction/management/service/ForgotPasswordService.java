package com.transaction.management.service;

import com.transaction.management.entity.ForgotPasswordOtp;
import com.transaction.management.entity.PasswordHistory;
import com.transaction.management.entity.User;
import com.transaction.management.repository.ForgotPasswordOtpRepository;
import com.transaction.management.repository.PasswordHistoryRepository;
import com.transaction.management.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ForgotPasswordService {

        private static final int OTP_EXPIRY_MINUTES = 5;
        private static final int RESEND_WAIT_SECONDS = 60;
        private static final int MAX_VERIFICATION_ATTEMPTS = 3;

        private final ForgotPasswordOtpRepository otpRepository;
        private final UserRepository userRepository;
        private final PasswordHistoryRepository passwordHistoryRepository;
        private final PasswordEncoder passwordEncoder;

        private final SecureRandom secureRandom = new SecureRandom();

        public ForgotPasswordService(
                        ForgotPasswordOtpRepository otpRepository,
                        UserRepository userRepository,
                        PasswordHistoryRepository passwordHistoryRepository,
                        PasswordEncoder passwordEncoder) {

                this.otpRepository = otpRepository;
                this.userRepository = userRepository;
                this.passwordHistoryRepository = passwordHistoryRepository;
                this.passwordEncoder = passwordEncoder;
        }

        public String generateOtp(String mobile) {

                User user = userRepository.findByMobile(mobile)
                                .orElseThrow(() -> new RuntimeException(
                                                "No account found with this mobile number"));

                if (!user.isActive()) {
                        throw new RuntimeException(
                                        "Account is inactive. Please use account recovery.");
                }

                LocalDateTime now = LocalDateTime.now();

                ForgotPasswordOtp previousOtp = otpRepository
                                .findTopByUserOrderByCreatedAtDesc(user)
                                .orElse(null);

                if (previousOtp != null &&
                                previousOtp.getResendAllowedAt().isAfter(now)) {

                        throw new RuntimeException(
                                        "Please wait before requesting another OTP");
                }

                String otp = String.format(
                                "%06d",
                                secureRandom.nextInt(1_000_000));

                ForgotPasswordOtp forgotPasswordOtp = new ForgotPasswordOtp();

                forgotPasswordOtp.setUser(user);
                forgotPasswordOtp.setOtp(otp);
                forgotPasswordOtp.setCreatedAt(now);
                forgotPasswordOtp.setExpiresAt(
                                now.plusMinutes(OTP_EXPIRY_MINUTES));
                forgotPasswordOtp.setResendAllowedAt(
                                now.plusSeconds(RESEND_WAIT_SECONDS));
                forgotPasswordOtp.setVerificationAttempts(0);
                forgotPasswordOtp.setUsed(false);

                otpRepository.save(forgotPasswordOtp);

                // Temporary development behavior.
                // Later this OTP will be sent through SMS.
                return otp;
        }

        public String verifyOtp(
                        String mobile,
                        String otp) {

                User user = userRepository.findByMobile(mobile)
                                .orElseThrow(() -> new RuntimeException(
                                                "No account found with this mobile number"));

                ForgotPasswordOtp forgotPasswordOtp = otpRepository
                                .findTopByUserOrderByCreatedAtDesc(user)
                                .orElseThrow(() -> new RuntimeException(
                                                "No OTP has been generated"));

                if (forgotPasswordOtp.isUsed()) {
                        throw new RuntimeException(
                                        "This OTP has already been used");
                }

                if (forgotPasswordOtp.getExpiresAt()
                                .isBefore(LocalDateTime.now())) {

                        throw new RuntimeException(
                                        "OTP has expired");
                }

                if (forgotPasswordOtp.getVerificationAttempts() >= MAX_VERIFICATION_ATTEMPTS) {

                        throw new RuntimeException(
                                        "Maximum OTP verification attempts exceeded");
                }

                if (!forgotPasswordOtp.getOtp().equals(otp)) {

                        forgotPasswordOtp.setVerificationAttempts(
                                        forgotPasswordOtp.getVerificationAttempts() + 1);

                        otpRepository.save(forgotPasswordOtp);

                        throw new RuntimeException(
                                        "Invalid OTP");
                }

                /*
                 * OTP is verified but not consumed yet.
                 *
                 * It will be marked as used only after the password
                 * has been successfully changed.
                 */
                forgotPasswordOtp.setVerified(true);

                otpRepository.save(forgotPasswordOtp);

                return "OTP verified successfully";
        }

        public String resetPassword(
                        String mobile,
                        String newPassword) {

                User user = userRepository.findByMobile(mobile)
                                .orElseThrow(() -> new RuntimeException(
                                                "No account found with this mobile number"));

                ForgotPasswordOtp forgotPasswordOtp = otpRepository
                                .findTopByUserOrderByCreatedAtDesc(user)
                                .orElseThrow(() -> new RuntimeException(
                                                "No OTP has been generated"));

                /*
                 * OTP must not already be used.
                 */
                if (forgotPasswordOtp.isUsed()) {
                        throw new RuntimeException(
                                        "This OTP has already been used");
                }
                /*
                 * OTP must have been successfully verified
                 * before the password can be reset.
                 */
                if (!forgotPasswordOtp.isVerified()) {
                        throw new RuntimeException(
                                        "Please verify the OTP before resetting your password");
                }

                /*
                 * OTP must still be valid.
                 */
                if (forgotPasswordOtp.getExpiresAt()
                                .isBefore(LocalDateTime.now())) {

                        throw new RuntimeException(
                                        "OTP has expired");
                }

                /*
                 * Validate the new password.
                 */
                if (!isValidPassword(newPassword)) {

                        throw new RuntimeException(
                                        "Password must contain at least 8 characters, "
                                                        + "one uppercase letter, "
                                                        + "one lowercase letter, "
                                                        + "one number, "
                                                        + "and one special character");
                }

                /*
                 * Prevent reuse of the current password.
                 */
                if (passwordEncoder.matches(
                                newPassword,
                                user.getPassword())) {

                        throw new RuntimeException(
                                        "New password cannot be the same as your current password");
                }

                /*
                 * Check all previously used passwords.
                 */
                List<PasswordHistory> previousPasswords = passwordHistoryRepository
                                .findByUserOrderByCreatedAtDesc(user);

                for (PasswordHistory oldPassword : previousPasswords) {

                        if (passwordEncoder.matches(
                                        newPassword,
                                        oldPassword.getPasswordHash())) {

                                throw new RuntimeException(
                                                "You cannot reuse a previously used password");
                        }
                }

                /*
                 * Save the CURRENT password into password history
                 * before replacing it.
                 */
                PasswordHistory passwordHistory = new PasswordHistory();

                passwordHistory.setUser(user);
                passwordHistory.setPasswordHash(
                                user.getPassword());
                passwordHistory.setCreatedAt(
                                LocalDateTime.now());

                passwordHistoryRepository.save(
                                passwordHistory);

                /*
                 * Set the new password using BCrypt hashing.
                 */
                user.setPassword(
                                passwordEncoder.encode(newPassword));

                user.setFailedLoginAttempts(0);
                user.setAccountLockedUntil(null);

                userRepository.save(user);

                /*
                 * Consume the OTP only after the password
                 * has been successfully changed.
                 */
                forgotPasswordOtp.setUsed(true);

                otpRepository.save(
                                forgotPasswordOtp);

                return "Password reset successfully";
        }

        private boolean isValidPassword(String password) {

                if (password == null ||
                                password.length() < 8) {

                        return false;
                }

                boolean hasUppercase = password.matches(".*[A-Z].*");

                boolean hasLowercase = password.matches(".*[a-z].*");

                boolean hasNumber = password.matches(".*[0-9].*");

                boolean hasSpecialCharacter = password.matches(".*[^a-zA-Z0-9].*");

                return hasUppercase
                                && hasLowercase
                                && hasNumber
                                && hasSpecialCharacter;
        }
}