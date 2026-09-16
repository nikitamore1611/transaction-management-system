package com.transaction.management.repository;

import com.transaction.management.entity.PasswordHistory;
import com.transaction.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordHistoryRepository
        extends JpaRepository<PasswordHistory, Long> {

    List<PasswordHistory> findByUserOrderByCreatedAtDesc(User user);
    void deleteByUser(User user);

}