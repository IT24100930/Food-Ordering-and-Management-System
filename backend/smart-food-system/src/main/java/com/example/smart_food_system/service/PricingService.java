package com.example.smart_food_system.service;

import com.example.smart_food_system.dto.order.OrderItemRequest;
import com.example.smart_food_system.dto.order.OrderTotals;
import com.example.smart_food_system.model.FoodOrder;
import com.example.smart_food_system.model.MenuItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class PricingService {

    private final BigDecimal taxRate;
    private final BigDecimal defaultDeliveryFee;

    public PricingService(@Value("${food.order.tax-rate:0.10}") BigDecimal taxRate,
                          @Value("${food.order.delivery-fee:250.00}") BigDecimal defaultDeliveryFee) {
        this.taxRate = taxRate;
        this.defaultDeliveryFee = defaultDeliveryFee;
    }

    public OrderTotals calculateTotals(List<OrderItemRequest> items,
                                       Map<Long, MenuItem> menuItems,
                                       FoodOrder.OrderType orderType,
                                       BigDecimal requestedDiscount,
                                       BigDecimal requestedDeliveryFee) {
        BigDecimal subtotal = items.stream()
                .map(item -> menuItems.get(item.getMenuItemId()).getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal roundedSubtotal = scale(subtotal);
        BigDecimal taxAmount = scale(roundedSubtotal.multiply(taxRate));
        BigDecimal discountAmount = scale(requestedDiscount == null ? BigDecimal.ZERO : requestedDiscount);
        BigDecimal deliveryFee = scale(resolveDeliveryFee(orderType, requestedDeliveryFee));
        BigDecimal totalAmount = scale(roundedSubtotal.add(taxAmount).add(deliveryFee).subtract(discountAmount));

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return OrderTotals.builder()
                .subtotal(roundedSubtotal)
                .taxAmount(taxAmount)
                .discountAmount(discountAmount)
                .deliveryFee(deliveryFee)
                .totalAmount(totalAmount)
                .build();
    }

    private BigDecimal resolveDeliveryFee(FoodOrder.OrderType orderType, BigDecimal requestedDeliveryFee) {
        if (requestedDeliveryFee != null) {
            return requestedDeliveryFee;
        }
        return orderType == FoodOrder.OrderType.DELIVERY ? defaultDeliveryFee : BigDecimal.ZERO;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
