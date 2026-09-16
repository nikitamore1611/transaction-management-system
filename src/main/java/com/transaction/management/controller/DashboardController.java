package com.transaction.management.controller;

import com.transaction.management.dto.DashboardRequest;
import com.transaction.management.dto.DashboardResponse;
import com.transaction.management.entity.User;
import com.transaction.management.repository.UserRepository;
import com.transaction.management.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    public DashboardController(
            DashboardService dashboardService,
            UserRepository userRepository) {

        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    // ==============================
    // Get Logged-In User
    // ==============================

    private User getLoggedInUser(
            Authentication authentication) {

        return userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(() ->
                new RuntimeException("User not found"));
    }

    // ==============================
    // Dashboard
    // ==============================

    @PostMapping
    public DashboardResponse getDashboard(
            @RequestBody DashboardRequest request,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        return dashboardService.getDashboard(
                user.getId(),
                request
        );
    }
}