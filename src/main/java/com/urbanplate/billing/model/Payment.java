package com.urbanplate.billing.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String paymentReference;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String transactionId; // From payment gateway

    @Column(length = 3)
    private String currency; // Currency code (LKR, USD, etc.)

    private BigDecimal exchangeRate; // Rate used if foreign currency

    private String cardLastFour;

    private String notes;

    @CreationTimestamp
    private LocalDateTime paymentDate;

    public enum PaymentMethod {
        CASH, CREDIT_CARD, DEBIT_CARD, PAYPAL, QR_CODE, MOBILE_MONEY, DIGITAL_WALLET
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED, CANCELLED
    }
}