package com.transaction.management.service;

import com.transaction.management.entity.AdminActionHistory;
import com.transaction.management.entity.User;
import com.transaction.management.repository.AdminActionHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminActionHistoryService {

    private final AdminActionHistoryRepository historyRepository;

    public AdminActionHistoryService(
            AdminActionHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public void recordAction(
            User adminUser,
            User targetUser,
            String action,
            String reason) {

        AdminActionHistory history =
                new AdminActionHistory();

        history.setAdminUser(adminUser);
        history.setTargetUser(targetUser);
        history.setAction(action);
        history.setReason(reason);
        history.setActionDateTime(LocalDateTime.now());

        historyRepository.save(history);
    }
}