package com.transaction.management.service;

import com.transaction.management.entity.User;
import com.transaction.management.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================
    // REGISTER USER
    // =========================================================

    public User registerUser(User user) {

        // Name validation
        if (user.getName() == null ||
                user.getName().trim().isEmpty()) {

            throw new RuntimeException("Name is required");
        }

        // Email validation
        if (user.getEmail() == null ||
                user.getEmail().trim().isEmpty()) {

            throw new RuntimeException("Email is required");
        }

        // Mobile validation
        if (user.getMobile() == null ||
                !user.getMobile().matches("\\d{10}")) {

            throw new RuntimeException(
                    "Mobile number must contain exactly 10 digits");
        }

        // Password validation
        if (!isValidPassword(user.getPassword())) {

            throw new RuntimeException(
                    "Password must contain at least 8 characters, "
                    + "one uppercase letter, "
                    + "one lowercase letter, "
                    + "one number, "
                    + "and one special character");
        }

        // Opening balance
        if (user.getOpeningBalance() == null) {

            user.setOpeningBalance(BigDecimal.ZERO);
        }

        // Opening balance cannot be negative
        if (user.getOpeningBalance()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Opening balance cannot be negative");
        }

        // Check unique email
        if (userRepository.existsByEmail(user.getEmail())) {

            throw new RuntimeException(
                    "Email is already registered");
        }

        // Check unique mobile
        if (userRepository.existsByMobile(user.getMobile())) {

            throw new RuntimeException(
                    "Mobile number is already registered");
        }

        // Hash password using BCrypt
        user.setPassword(
                passwordEncoder.encode(user.getPassword()));

        // Set account details
        user.setRegistrationDate(
                LocalDateTime.now());

        user.setActive(true);

        user.setRole("USER");

        user.setFailedLoginAttempts(0);

        return userRepository.save(user);
    }


    // =========================================================
    // LOGIN USER
    // =========================================================

    public User loginUser(
            String email,
            String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid email or password"));

        if (!user.isActive()) {

            throw new RuntimeException(
                    "Account is inactive");
        }

        if (!passwordEncoder.matches(
                password,
                user.getPassword())) {

            throw new RuntimeException(
                    "Invalid email or password");
        }

        user.setLastLogin(
                LocalDateTime.now());

        user.setFailedLoginAttempts(0);

        return userRepository.save(user);
    }


    // =========================================================
    // GET USER BY EMAIL
    // =========================================================

    public User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));
    }


    // =========================================================
    // PASSWORD VALIDATION
    // =========================================================

    private boolean isValidPassword(String password) {

        if (password == null ||
                password.length() < 8) {

            return false;
        }

        boolean hasUppercase =
                password.matches(".*[A-Z].*");

        boolean hasLowercase =
                password.matches(".*[a-z].*");

        boolean hasNumber =
                password.matches(".*[0-9].*");

        boolean hasSpecialCharacter =
                password.matches(".*[^a-zA-Z0-9].*");

        return hasUppercase
                && hasLowercase
                && hasNumber
                && hasSpecialCharacter;
    }
}