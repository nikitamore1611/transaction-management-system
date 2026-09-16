package com.transaction.management.dto;

import java.time.LocalDateTime;

public class AdminActionHistoryResponse {

    private Long id;

    private Long adminUserId;
    private String adminName;

    private Long targetUserId;
    private String targetUserName;

    private String action;
    private String reason;
    private LocalDateTime actionDateTime;

    public AdminActionHistoryResponse() {
    }

    public AdminActionHistoryResponse(
            Long id,
            Long adminUserId,
            String adminName,
            Long targetUserId,
            String targetUserName,
            String action,
            String reason,
            LocalDateTime actionDateTime) {

        this.id = id;
        this.adminUserId = adminUserId;
        this.adminName = adminName;
        this.targetUserId = targetUserId;
        this.targetUserName = targetUserName;
        this.action = action;
        this.reason = reason;
        this.actionDateTime = actionDateTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(Long adminUserId) {
        this.adminUserId = adminUserId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public void setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getActionDateTime() {
        return actionDateTime;
    }

    public void setActionDateTime(LocalDateTime actionDateTime) {
        this.actionDateTime = actionDateTime;
    }
}