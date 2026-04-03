package com.example.smart_food_system.dto.order;

import com.example.smart_food_system.model.FoodOrder;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private FoodOrder.OrderType orderType;
    private FoodOrder.OrderStatus status;
    private FoodOrder.PaymentStatus paymentStatus;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private String notes;
    private String tableNumber;
    private String deliveryAddress;
    private Integer itemCount;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
