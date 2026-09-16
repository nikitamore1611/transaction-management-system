package com.transaction.management.controller;

import com.transaction.management.dto.AdminActionHistoryResponse;
import com.transaction.management.service.AdminActionHistoryViewService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/action-history")
public class AdminActionHistoryController {

    private final AdminActionHistoryViewService historyViewService;

    public AdminActionHistoryController(
            AdminActionHistoryViewService historyViewService) {

        this.historyViewService = historyViewService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminActionHistoryResponse> getActionHistory() {

        return historyViewService.getAllHistory();
    }
}