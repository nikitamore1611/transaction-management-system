package com.transaction.management.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "forgot_password_otps")
public class ForgotPasswordOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime resendAllowedAt;

    @Column(nullable = false)
    private int verificationAttempts = 0;

    @Column(nullable = false)
    private boolean used = false;
    @Column(nullable = false)
    private boolean verified = false;

    public ForgotPasswordOtp() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getOtp() {
        return otp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getResendAllowedAt() {
        return resendAllowedAt;
    }

    public int getVerificationAttempts() {
        return verificationAttempts;
    }

    public boolean isUsed() {
        return used;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setResendAllowedAt(LocalDateTime resendAllowedAt) {
        this.resendAllowedAt = resendAllowedAt;
    }

    public void setVerificationAttempts(int verificationAttempts) {
        this.verificationAttempts = verificationAttempts;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
    public boolean isVerified() {
    return verified;
}

public void setVerified(boolean verified) {
    this.verified = verified;
}
}