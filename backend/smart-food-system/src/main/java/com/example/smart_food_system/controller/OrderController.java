package com.example.smart_food_system.controller;

import com.example.smart_food_system.dto.ApiResponse;
import com.example.smart_food_system.dto.order.CreateOrderRequest;
import com.example.smart_food_system.dto.order.UpdateOrderRequest;
import com.example.smart_food_system.dto.order.UpdateOrderStatusRequest;
import com.example.smart_food_system.model.FoodOrder;
import com.example.smart_food_system.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse> getOrders(@RequestParam(required = false) String query,
                                                 @RequestParam(required = false) FoodOrder.OrderStatus status,
                                                 @RequestParam(required = false) FoodOrder.OrderType orderType) {
        return ResponseEntity.ok(ApiResponse.ok("Orders fetched successfully",
                orderService.getAll(query, status, orderType)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Order fetched successfully", orderService.getById(id)));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<ApiResponse> getOrderItems(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Order items fetched successfully", orderService.getItems(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order created successfully", orderService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateOrder(@PathVariable Long id, @Valid @RequestBody UpdateOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Order updated successfully", orderService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Order status updated successfully", orderService.updateStatus(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled successfully", orderService.cancel(id)));
    }
}
