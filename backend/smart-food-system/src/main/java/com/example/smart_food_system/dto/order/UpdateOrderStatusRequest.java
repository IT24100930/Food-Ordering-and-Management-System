package com.example.smart_food_system.dto.order;

import com.example.smart_food_system.model.FoodOrder;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotNull
    private FoodOrder.OrderStatus status;
}
