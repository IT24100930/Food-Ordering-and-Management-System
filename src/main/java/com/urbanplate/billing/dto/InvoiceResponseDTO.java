package com.urbanplate.billing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InvoiceResponseDTO {
    private Long id;
    private String invoiceNumber;
    private Long orderId;
    private Long customerId;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String status;
    private String qrCode;
    private LocalDateTime createdAt;
    private BigDecimal paidAmount;
    private BigDecimal remainingBalance;
}