package com.urbanplate.billing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class AnalyticsDTO {
    private BigDecimal todayRevenue;
    private BigDecimal weeklyRevenue;
    private long totalInvoices;
    private long paidInvoices;
    private Map<String, Long> paymentMethodDistribution;
    private BigDecimal averageOrderValue;
}