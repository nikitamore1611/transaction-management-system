package com.transaction.management.repository;

import com.transaction.management.entity.AdminActionHistory;
import com.transaction.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminActionHistoryRepository
        extends JpaRepository<AdminActionHistory, Long> {

    List<AdminActionHistory>
    findByTargetUserOrderByActionDateTimeDesc(User targetUser);

    List<AdminActionHistory>
    findAllByOrderByActionDateTimeDesc();
    void deleteByTargetUser(User targetUser);
}