package com.example.smart_food_system.repository;

import com.example.smart_food_system.model.RoleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleHistoryRepository extends JpaRepository<RoleHistory, Long> {

    // All role changes for one user, newest first
    List<RoleHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    // All system-triggered promotions
    List<RoleHistory> findByChangedByOrderByCreatedAtDesc(String changedBy);
}
