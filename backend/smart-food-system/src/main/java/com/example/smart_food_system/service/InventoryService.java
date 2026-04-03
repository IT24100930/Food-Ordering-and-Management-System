package com.example.smart_food_system.service;

import com.example.smart_food_system.exception.BadRequestException;
import com.example.smart_food_system.model.FoodOrder;
import com.example.smart_food_system.model.MenuItem;
import com.example.smart_food_system.model.OrderItem;
import com.example.smart_food_system.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final MenuItemRepository menuItemRepository;

    public void validateStock(MenuItem menuItem, int quantity) {
        if (menuItem.getStockQty() != null && menuItem.getStockQty() > 0 && quantity > menuItem.getStockQty()) {
            throw new BadRequestException(menuItem.getName() + " only has " + menuItem.getStockQty() + " item(s) in stock.");
        }
    }

    public void applyInventoryHook(FoodOrder order, FoodOrder.OrderStatus previousStatus, FoodOrder.OrderStatus nextStatus) {
        boolean shouldDeduct = (nextStatus == FoodOrder.OrderStatus.CONFIRMED || nextStatus == FoodOrder.OrderStatus.COMPLETED)
                && previousStatus != FoodOrder.OrderStatus.CONFIRMED
                && previousStatus != FoodOrder.OrderStatus.COMPLETED;

        if (!shouldDeduct) {
            return;
        }

        for (OrderItem item : order.getItems()) {
            MenuItem menuItem = item.getMenuItem();
            if (menuItem.getStockQty() != null && menuItem.getStockQty() > 0) {
                validateStock(menuItem, item.getQuantity());
                menuItem.setStockQty(menuItem.getStockQty() - item.getQuantity());
                menuItemRepository.save(menuItem);
            }
        }
    }
}
