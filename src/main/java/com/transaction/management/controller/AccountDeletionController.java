package com.transaction.management.controller;

import com.transaction.management.entity.User;
import com.transaction.management.repository.UserRepository;
import com.transaction.management.service.AccountDeletionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountDeletionController {

    private final UserRepository userRepository;
    private final AccountDeletionService accountDeletionService;

    public AccountDeletionController(
            UserRepository userRepository,
            AccountDeletionService accountDeletionService) {

        this.userRepository = userRepository;
        this.accountDeletionService = accountDeletionService;
    }

    @DeleteMapping("/delete")
    public String deleteAccount(
            Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Admin accounts cannot be deleted through this endpoint
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException(
                    "Admin account cannot be deleted");
        }

        accountDeletionService.deleteAccount(user);

        return "Account deleted successfully";
    }
}