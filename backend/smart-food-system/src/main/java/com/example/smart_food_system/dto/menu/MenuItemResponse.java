package com.example.smart_food_system.dto.menu;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MenuItemResponse {
    private Long id;
    private String itemCode;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private BigDecimal costPrice;
    private String imageUrl;
    private Boolean isAvailable;
    private Integer preparationTime;
    private Integer stockQty;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
