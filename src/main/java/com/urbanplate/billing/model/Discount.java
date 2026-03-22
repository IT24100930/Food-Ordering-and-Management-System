package com.urbanplate.billing.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "discounts", schema = "billing")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coupon_code", unique = true, nullable = false, length = 50)
    private String couponCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(length = 200)
    private String description;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "minimum_order_amount", precision = 10, scale = 2)
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT
    }

    // Check if discount is currently valid
    public boolean isValid() {
        LocalDate now = LocalDate.now();
        return active &&
                now.isAfter(validFrom) &&
                now.isBefore(validUntil) &&
                (usageLimit == null || usageCount < usageLimit);
    }

    // Calculate discount amount based on subtotal
    public BigDecimal calculateDiscount(BigDecimal amount) {
        if (type == DiscountType.PERCENTAGE) {
            return amount.multiply(value).divide(BigDecimal.valueOf(100));
        } else {
            // Fixed amount discount cannot exceed the amount
            return value.min(amount);
        }
    }
}