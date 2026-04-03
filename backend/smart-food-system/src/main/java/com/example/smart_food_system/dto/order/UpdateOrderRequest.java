package com.example.smart_food_system.dto.order;

import com.example.smart_food_system.model.FoodOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateOrderRequest {
    @NotBlank
    @Size(max = 120)
    private String customerName;

    @NotBlank
    @Size(max = 30)
    private String customerPhone;

    @Email
    @Size(max = 150)
    private String customerEmail;

    @NotNull
    private FoodOrder.OrderType orderType;

    @NotNull
    private FoodOrder.PaymentStatus paymentStatus;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal deliveryFee;

    @Size(max = 1000)
    private String notes;

    @Size(max = 30)
    private String tableNumber;

    @Size(max = 500)
    private String deliveryAddress;

    @Valid
    @NotEmpty
    private List<OrderItemRequest> items;
}
