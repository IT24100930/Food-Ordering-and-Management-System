package com.example.smart_food_system.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull
    private Long menuItemId;

    @NotNull
    @Min(1)
    private Integer quantity;

    @Size(max = 500)
    private String notes;
}
