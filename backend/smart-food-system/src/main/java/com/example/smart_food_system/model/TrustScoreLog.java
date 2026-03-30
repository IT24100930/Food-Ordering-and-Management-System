package com.example.smart_food_system.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "trust_score_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrustScoreLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "old_score")
    private Integer oldScore;

    @Column(name = "new_score")
    private Integer newScore;

    // positive = gained, negative = lost
    @Column(name = "change_by")
    private Integer changeBy;

    @Column(nullable = false)
    private String reason;

    // e.g. ORDER_COMPLETE, CANCELLATION, COMPLAINT, MANUAL_ADJUST
    @Column(name = "action_type")
    private String actionType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}