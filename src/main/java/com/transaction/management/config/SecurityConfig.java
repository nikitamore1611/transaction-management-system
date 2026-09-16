package com.transaction.management.config;

import com.transaction.management.security.CustomAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomAuthenticationProvider customAuthenticationProvider;

    public SecurityConfig(
            CustomAuthenticationProvider customAuthenticationProvider) {

        this.customAuthenticationProvider =
                customAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {

        return new org.springframework.security.authentication.ProviderManager(
                java.util.List.of(customAuthenticationProvider)
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authenticationProvider(
                        customAuthenticationProvider
                )

                .authorizeHttpRequests(auth -> auth

                        // Public pages and APIs
                        .requestMatchers(
                                "/",
                                "/login.html",
                                "/register.html",
                                "/report.html",
                                "/forgot-password.html",
                                "/account-recovery.html",

                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",

                                "/api/users/register",
                                "/api/users/login",

                                "/api/account-recovery/send-otp",
                                "/api/account-recovery/verify-otp",

                                "/api/forgot-password/send-otp",
                                "/api/forgot-password/verify-otp",
                                "/api/forgot-password/reset-password"

                        )
                        .permitAll()

                        // Admin page is accessible only to ADMIN
                        .requestMatchers("/admin.html")
                        .hasRole("ADMIN")

                        // Everything else requires login
                        .anyRequest()
                        .authenticated()
                )

                .formLogin(form -> form.disable());

        return http.build();
    }
}