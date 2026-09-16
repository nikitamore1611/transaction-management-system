package com.transaction.management.security;

import com.transaction.management.entity.User;
import com.transaction.management.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

        private static final int USER_MAX_FAILED_ATTEMPTS = 5;
        private static final int ADMIN_MAX_FAILED_ATTEMPTS = 3;
        private static final long LOCK_HOURS = 24;

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        public CustomAuthenticationProvider(
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder) {

                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
        }

        @Override
        public Authentication authenticate(
                        Authentication authentication)
                        throws AuthenticationException {

                String email = authentication.getName();
                String password = authentication.getCredentials().toString();

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new BadCredentialsException(
                                                "Invalid email or password"));

                // Automatically deactivate the account
                // if the user has not logged in for 1 year
                if (user.getLastLogin() != null &&
                                user.getLastLogin()
                                                .isBefore(LocalDateTime.now().minusYears(1))) {

                        user.setActive(false);
                        userRepository.save(user);

                        throw new DisabledException(
                                        "Account has been deactivated due to 1 year of inactivity");
                }

                // Check whether the account is active
                if (!user.isActive()) {
                        throw new DisabledException(
                                        "Account is inactive");
                }
                // Check whether the account is currently locked
                if (user.getAccountLockedUntil() != null) {

                        if (user.getAccountLockedUntil()
                                        .isAfter(LocalDateTime.now())) {

                                throw new LockedException(
                                                "Account is locked until "
                                                                + user.getAccountLockedUntil());
                        }

                        // Lock period has expired
                        user.setAccountLockedUntil(null);
                        user.setFailedLoginAttempts(0);

                        userRepository.save(user);
                }

                // Check password
                if (!passwordEncoder.matches(
                                password,
                                user.getPassword())) {

                        int failedAttempts = user.getFailedLoginAttempts() + 1;

                        user.setFailedLoginAttempts(
                                        failedAttempts);

                        int maxFailedAttempts = "ADMIN".equalsIgnoreCase(user.getRole())
                                        ? ADMIN_MAX_FAILED_ATTEMPTS
                                        : USER_MAX_FAILED_ATTEMPTS;

                        if (failedAttempts >= maxFailedAttempts) {
                                user.setAccountLockedUntil(
                                                LocalDateTime.now()
                                                                .plusHours(LOCK_HOURS));

                                userRepository.save(user);

                                throw new LockedException(
                                                "Too many failed login attempts. "
                                                                + "Your account has been locked for 24 hours.");
                        }

                        userRepository.save(user);

                        throw new BadCredentialsException(
                                        "Invalid email or password");
                }

                // Successful login
                user.setFailedLoginAttempts(0);
                user.setAccountLockedUntil(null);
                user.setLastLogin(LocalDateTime.now());

                userRepository.save(user);

                return new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                null,
                                List.of(
                                                new SimpleGrantedAuthority(
                                                                "ROLE_" + user.getRole())));
        }

        @Override
        public boolean supports(Class<?> authentication) {

                return UsernamePasswordAuthenticationToken.class
                                .isAssignableFrom(authentication);
        }
}