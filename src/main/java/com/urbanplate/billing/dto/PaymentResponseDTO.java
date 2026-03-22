package com.urbanplate.billing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponseDTO {
    private Long id;
    private String paymentReference;
    private Long invoiceId;
    private BigDecimal amount;
    private String method;
    private String status;
    private String transactionId;
    private String currency;
    private LocalDateTime paymentDate;
    private String cardLastFour;
    private String notes;
}