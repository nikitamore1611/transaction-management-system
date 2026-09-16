package com.transaction.management.dto;

public class AdminUserStatusRequest {

    private boolean active;
    private String reason;

    public AdminUserStatusRequest() {
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}