package com.transaction.management.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_action_history")
public class AdminActionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Admin who performed the action
    @ManyToOne
    @JoinColumn(name = "admin_user_id", nullable = false)
    private User adminUser;

    // Normal user affected by the action
    @ManyToOne
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    // ACTIVATE or DEACTIVATE
    @Column(nullable = false)
    private String action;

    // Mandatory reason
    @Column(nullable = false, length = 255)
    private String reason;

    // Date and time of the action
    @Column(nullable = false)
    private LocalDateTime actionDateTime;

    public AdminActionHistory() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(User adminUser) {
        this.adminUser = adminUser;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
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