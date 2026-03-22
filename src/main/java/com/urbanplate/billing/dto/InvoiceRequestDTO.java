package com.urbanplate.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceRequestDTO {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Order total is required")
    @Positive(message = "Order total must be positive")
    private BigDecimal orderTotal;

    private String couponCode;

    private String notes;
}