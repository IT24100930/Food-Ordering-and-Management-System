package com.example.smart_food_system.repository;

import com.example.smart_food_system.model.TrustScoreLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrustScoreLogRepository extends JpaRepository<TrustScoreLog, Long> {

    // Score history for one user, newest first
    List<TrustScoreLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}