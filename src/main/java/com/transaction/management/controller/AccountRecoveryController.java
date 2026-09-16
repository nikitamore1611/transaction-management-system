package com.transaction.management.controller;

import com.transaction.management.service.AccountRecoveryOtpService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account-recovery")
public class AccountRecoveryController {

    private final AccountRecoveryOtpService
            accountRecoveryOtpService;

    public AccountRecoveryController(
            AccountRecoveryOtpService accountRecoveryOtpService) {

        this.accountRecoveryOtpService =
                accountRecoveryOtpService;
    }

    @PostMapping("/send-otp")
    public String sendOtp(
            @RequestParam String mobile) {

        return accountRecoveryOtpService
                .generateOtpForInactiveUser(mobile);
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam String mobile,
            @RequestParam String otp) {

        return accountRecoveryOtpService
                .verifyOtp(mobile, otp);
    }
}