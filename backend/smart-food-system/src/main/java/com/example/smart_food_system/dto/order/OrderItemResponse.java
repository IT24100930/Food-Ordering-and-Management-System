package com.example.smart_food_system.dto.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private Long menuItemId;
    private String itemNameSnapshot;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
    private String notes;
}
