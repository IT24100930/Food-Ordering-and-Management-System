package com.example.smart_food_system.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
 
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
 
    @Column(nullable = false, unique = true, length = 100)
    private String email;
 
    @Column(nullable = false)
    private String password;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.CUSTOMER;
 
    @Column(length = 500)
    private String address;
 
    @Column(length = 20)
    private String telephone;
 
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.ACTIVE;
 
    @Column(name = "reset_token")
    private String resetToken;
 
    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;
 
    // ── TRUST SCORE FIELDS 
    @Column(name = "trust_score")
    @Builder.Default
    private Integer trustScore = 75;
 
    @Column(name = "trust_level", length = 20)
    @Builder.Default
    private String trustLevel = "NORMAL";
 
    // Customer metrics
    @Column(name = "total_orders")
    @Builder.Default
    private Integer totalOrders = 0;
 
    @Column(name = "total_spending")
    @Builder.Default
    private BigDecimal totalSpending = BigDecimal.ZERO;
 
    @Column(name = "cancellations")
    @Builder.Default
    private Integer cancellations = 0;
 
    // Staff metrics
    @Column(name = "completed_tasks")
    @Builder.Default
    private Integer completedTasks = 0;
 
    @Column(name = "performance_rating")
    @Builder.Default
    private Double performanceRating = 0.0;
 
    @Column(name = "complaints_count")
    @Builder.Default
    private Integer complaintsCount = 0;
 
    // Restriction flag
    @Column(name = "is_restricted")
    @Builder.Default
    private Boolean isRestricted = false;
    
 
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
 
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
 
    // ── ENUMS 
    public enum Role { ADMIN, STAFF, CUSTOMER }
    public enum Status { ACTIVE, INACTIVE }
 
    // Trust level labels
    public static String calcTrustLevel(int score) {
        if (score >= 80) return "HIGH";
        if (score >= 60) return "NORMAL";
        if (score >= 40) return "WARNING";
        return "RESTRICTED";
    }
}