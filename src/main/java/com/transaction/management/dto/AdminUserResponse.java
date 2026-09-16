package com.transaction.management.dto;

import java.time.LocalDateTime;

public class AdminUserResponse {

    private Long id;
    private String name;
    private String email;
    private String mobile;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLogin;
    private boolean active;
    private String role;

    public AdminUserResponse() {
    }

    public AdminUserResponse(
            Long id,
            String name,
            String email,
            String mobile,
            LocalDateTime registrationDate,
            LocalDateTime lastLogin,
            boolean active,
            String role) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.registrationDate = registrationDate;
        this.lastLogin = lastLogin;
        this.active = active;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}