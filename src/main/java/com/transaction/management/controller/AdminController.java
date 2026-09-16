package com.transaction.management.controller;

import com.transaction.management.dto.AdminUserResponse;
import com.transaction.management.dto.AdminUserStatusRequest;
import com.transaction.management.entity.User;
import com.transaction.management.repository.UserRepository;
import com.transaction.management.service.AdminActionHistoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final AdminActionHistoryService adminActionHistoryService;

    public AdminController(
            UserRepository userRepository,
            AdminActionHistoryService adminActionHistoryService) {

        this.userRepository = userRepository;
        this.adminActionHistoryService = adminActionHistoryService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminUserResponse> getUsers() {

        return userRepository.findAll()
                .stream()
                .map(user -> new AdminUserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getMobile(),
                        user.getRegistrationDate(),
                        user.getLastLogin(),
                        user.isActive(),
                        user.getRole()))
                    .toList();
    }

    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUserStatus(
            @PathVariable Long userId,
            @RequestBody AdminUserStatusRequest request,
            org.springframework.security.core.Authentication authentication) {

        User adminUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException(
                        "Admin user not found"));

        User targetUser = userRepository
                .findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found"));

        // Admin accounts cannot be activated or deactivated
        if ("ADMIN".equalsIgnoreCase(targetUser.getRole())) {
            throw new RuntimeException(
                    "Admin accounts cannot be modified");
        }

        if (request.getReason() == null ||
                request.getReason().trim().isEmpty()) {

            throw new RuntimeException(
                    "Reason is mandatory");
        }

        targetUser.setActive(request.isActive());

        userRepository.save(targetUser);

        String action;

        if (request.isActive()) {
            action = "ACTIVATE";
        } else {
            action = "DEACTIVATE";
        }

        adminActionHistoryService.recordAction(
                adminUser,
                targetUser,
                action,
                request.getReason().trim());

        if (request.isActive()) {
            return "User activated successfully";
        } else {
            return "User deactivated successfully";
        }
    }
}