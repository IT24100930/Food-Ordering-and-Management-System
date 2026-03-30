package com.example.smart_food_system.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "role_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "old_role", nullable = false)
    private String oldRole;

    @Column(name = "new_role", nullable = false)
    private String newRole;

    // "SYSTEM" = auto-promoted, "ADMIN" = manual
    @Column(name = "changed_by")
    @Builder.Default
    private String changedBy = "SYSTEM";

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "trust_score_at")
    private Integer trustScoreAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}