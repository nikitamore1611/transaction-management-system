package com.transaction.management.controller;

import com.transaction.management.service.ForgotPasswordService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forgot-password")
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    public ForgotPasswordController(
            ForgotPasswordService forgotPasswordService) {

        this.forgotPasswordService = forgotPasswordService;
    }

    @PostMapping("/send-otp")
    public String sendOtp(
            @RequestParam String mobile) {

        return forgotPasswordService.generateOtp(mobile);
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam String mobile,
            @RequestParam String otp) {

        return forgotPasswordService.verifyOtp(
                mobile,
                otp
        );
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String mobile,
            @RequestParam String newPassword) {

        return forgotPasswordService.resetPassword(
                mobile,
                newPassword
        );
    }
}