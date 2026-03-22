package com.urbanplate.billing.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "taxes", schema = "billing")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tax {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tax_name", unique = true, nullable = false, length = 50)
    private String taxName;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal rate;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicable_to", nullable = false)
    private TaxCategory applicableTo;

    private Boolean active = true;

    @Column(length = 200)
    private String description;

    public enum TaxCategory {
        ALL, FOOD, BEVERAGE, SERVICE
    }

    public BigDecimal calculateTax(BigDecimal amount) {
        return amount.multiply(rate).divide(BigDecimal.valueOf(100));
    }
}