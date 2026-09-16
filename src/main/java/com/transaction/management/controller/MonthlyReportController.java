package com.transaction.management.controller;

import com.transaction.management.dto.MonthlyReportResponse;
import com.transaction.management.entity.User;
import com.transaction.management.repository.UserRepository;
import com.transaction.management.service.MonthlyReportService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class MonthlyReportController {

    private final MonthlyReportService monthlyReportService;
    private final UserRepository userRepository;

    public MonthlyReportController(
            MonthlyReportService monthlyReportService,
            UserRepository userRepository) {

        this.monthlyReportService = monthlyReportService;
        this.userRepository = userRepository;
    }

    private User getLoggedInUser(
            Authentication authentication) {

        return userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(() ->
                new RuntimeException("User not found"));
    }

    @GetMapping("/monthly")
    public MonthlyReportResponse getMonthlyReport(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        return monthlyReportService.generateMonthlyReport(
                user.getId(),
                year,
                month
        );
    }
}