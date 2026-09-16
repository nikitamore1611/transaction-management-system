package com.transaction.management.service;

import com.transaction.management.dto.AdminActionHistoryResponse;
import com.transaction.management.entity.AdminActionHistory;
import com.transaction.management.repository.AdminActionHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminActionHistoryViewService {

    private final AdminActionHistoryRepository historyRepository;

    public AdminActionHistoryViewService(
            AdminActionHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public List<AdminActionHistoryResponse> getAllHistory() {

        return historyRepository
                .findAllByOrderByActionDateTimeDesc()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    private AdminActionHistoryResponse convertToResponse(
            AdminActionHistory history) {

        return new AdminActionHistoryResponse(
                history.getId(),

                history.getAdminUser().getId(),
                history.getAdminUser().getName(),

                history.getTargetUser().getId(),
                history.getTargetUser().getName(),

                history.getAction(),
                history.getReason(),
                history.getActionDateTime()
        );
    }
}