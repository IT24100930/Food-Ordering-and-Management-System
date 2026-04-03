package com.example.smart_food_system.service;

import com.example.smart_food_system.dto.order.*;
import com.example.smart_food_system.exception.BadRequestException;
import com.example.smart_food_system.exception.InvalidStatusTransitionException;
import com.example.smart_food_system.exception.NotFoundException;
import com.example.smart_food_system.model.FoodOrder;
import com.example.smart_food_system.model.MenuItem;
import com.example.smart_food_system.model.OrderItem;
import com.example.smart_food_system.repository.FoodOrderRepository;
import com.example.smart_food_system.repository.MenuItemRepository;
import com.example.smart_food_system.repository.OrderItemRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final Map<FoodOrder.OrderStatus, Set<FoodOrder.OrderStatus>> VALID_TRANSITIONS = Map.of(
            FoodOrder.OrderStatus.PENDING, Set.of(FoodOrder.OrderStatus.CONFIRMED, FoodOrder.OrderStatus.CANCELLED),
            FoodOrder.OrderStatus.CONFIRMED, Set.of(FoodOrder.OrderStatus.PREPARING, FoodOrder.OrderStatus.CANCELLED),
            FoodOrder.OrderStatus.PREPARING, Set.of(FoodOrder.OrderStatus.READY, FoodOrder.OrderStatus.CANCELLED),
            FoodOrder.OrderStatus.READY, Set.of(FoodOrder.OrderStatus.COMPLETED),
            FoodOrder.OrderStatus.COMPLETED, Set.of(),
            FoodOrder.OrderStatus.CANCELLED, Set.of()
    );

    private final FoodOrderRepository foodOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final PricingService pricingService;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public List<OrderResponse> getAll(String query, FoodOrder.OrderStatus status, FoodOrder.OrderType orderType) {
        Specification<FoodOrder> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null && !query.isBlank()) {
                String like = "%" + query.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("orderNumber")), like),
                        cb.like(cb.lower(root.get("customerName")), like),
                        cb.like(cb.lower(root.get("customerPhone")), like)
                ));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (orderType != null) {
                predicates.add(cb.equal(root.get("orderType"), orderType));
            }
            cq.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return foodOrderRepository.findAll(spec).stream()
                .sorted(Comparator.comparing(FoodOrder::getCreatedAt).reversed())
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        return toDetailedResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<OrderItemResponse> getItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId).stream().map(this::toItemResponse).toList();
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        FoodOrder order = FoodOrder.builder()
                .orderNumber(generateOrderNumber())
                .status(FoodOrder.OrderStatus.PENDING)
                .paymentStatus(request.getPaymentStatus() == null ? FoodOrder.PaymentStatus.UNPAID : request.getPaymentStatus())
                .build();
        applyAndSave(order, request.getCustomerName(), request.getCustomerPhone(), request.getCustomerEmail(), request.getOrderType(),
                request.getPaymentStatus() == null ? FoodOrder.PaymentStatus.UNPAID : request.getPaymentStatus(),
                request.getDiscountAmount(), request.getDeliveryFee(), request.getNotes(), request.getTableNumber(),
                request.getDeliveryAddress(), request.getItems());
        return toDetailedResponse(order);
    }

    @Transactional
    public OrderResponse update(Long id, UpdateOrderRequest request) {
        FoodOrder order = findEntity(id);
        if (order.getStatus() == FoodOrder.OrderStatus.COMPLETED || order.getStatus() == FoodOrder.OrderStatus.CANCELLED) {
            throw new BadRequestException("Completed or cancelled orders cannot be edited");
        }
        applyAndSave(order, request.getCustomerName(), request.getCustomerPhone(), request.getCustomerEmail(), request.getOrderType(),
                request.getPaymentStatus(), request.getDiscountAmount(), request.getDeliveryFee(), request.getNotes(),
                request.getTableNumber(), request.getDeliveryAddress(), request.getItems());
        return toDetailedResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request) {
        FoodOrder order = findEntity(id);
        FoodOrder.OrderStatus currentStatus = order.getStatus();
        FoodOrder.OrderStatus nextStatus = request.getStatus();

        if (currentStatus == nextStatus) {
            return toDetailedResponse(order);
        }
        if (!VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(nextStatus)) {
            throw new InvalidStatusTransitionException("Invalid status transition from " + currentStatus + " to " + nextStatus);
        }

        inventoryService.applyInventoryHook(order, currentStatus, nextStatus);
        order.setStatus(nextStatus);
        foodOrderRepository.save(order);
        log.info("Updated order {} from {} to {}", order.getOrderNumber(), currentStatus, nextStatus);
        return toDetailedResponse(order);
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(FoodOrder.OrderStatus.CANCELLED);
        return updateStatus(id, request);
    }

    private void applyAndSave(FoodOrder order, String customerName, String customerPhone, String customerEmail,
                              FoodOrder.OrderType orderType, FoodOrder.PaymentStatus paymentStatus,
                              BigDecimal discountAmount, BigDecimal deliveryFee, String notes,
                              String tableNumber, String deliveryAddress, List<OrderItemRequest> items) {
        validateRequest(orderType, tableNumber, deliveryAddress, items);
        Map<Long, MenuItem> menuItemMap = resolveMenuItems(items);
        OrderTotals totals = pricingService.calculateTotals(items, menuItemMap, orderType, discountAmount, deliveryFee);

        order.setCustomerName(customerName.trim());
        order.setCustomerPhone(customerPhone.trim());
        order.setCustomerEmail(customerEmail == null || customerEmail.isBlank() ? null : customerEmail.trim());
        order.setOrderType(orderType);
        order.setPaymentStatus(paymentStatus);
        order.setNotes(notes);
        order.setTableNumber(orderType == FoodOrder.OrderType.DINE_IN ? tableNumber : null);
        order.setDeliveryAddress(orderType == FoodOrder.OrderType.DELIVERY ? deliveryAddress : null);
        order.setSubtotal(totals.getSubtotal());
        order.setTaxAmount(totals.getTaxAmount());
        order.setDiscountAmount(totals.getDiscountAmount());
        order.setDeliveryFee(totals.getDeliveryFee());
        order.setTotalAmount(totals.getTotalAmount());

        order.getItems().clear();
        for (OrderItemRequest itemRequest : items) {
            MenuItem menuItem = menuItemMap.get(itemRequest.getMenuItemId());
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .itemNameSnapshot(menuItem.getName())
                    .unitPrice(menuItem.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .lineTotal(menuItem.getPrice()
                            .multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                            .setScale(2, RoundingMode.HALF_UP))
                    .notes(itemRequest.getNotes())
                    .build();
            order.getItems().add(orderItem);
        }

        foodOrderRepository.save(order);
    }

    private void validateRequest(FoodOrder.OrderType orderType, String tableNumber, String deliveryAddress, List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("An order must contain at least one item");
        }
        if (orderType == FoodOrder.OrderType.DELIVERY && (deliveryAddress == null || deliveryAddress.isBlank())) {
            throw new BadRequestException("Delivery address is required for delivery orders");
        }
        if (orderType == FoodOrder.OrderType.DINE_IN && (tableNumber == null || tableNumber.isBlank())) {
            throw new BadRequestException("Table number is required for dine-in orders");
        }
    }

    private Map<Long, MenuItem> resolveMenuItems(List<OrderItemRequest> items) {
        Set<Long> ids = new LinkedHashSet<>();
        for (OrderItemRequest item : items) {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BadRequestException("Quantity must be greater than zero");
            }
            ids.add(item.getMenuItemId());
        }

        List<MenuItem> menuItems = menuItemRepository.findAllById(ids);
        if (menuItems.size() != ids.size()) {
            throw new NotFoundException("One or more menu items were not found");
        }

        Map<Long, MenuItem> map = new HashMap<>();
        for (MenuItem menuItem : menuItems) {
            if (Boolean.TRUE.equals(menuItem.getDeleted())) {
                throw new NotFoundException("One or more menu items were not found");
            }
            if (!Boolean.TRUE.equals(menuItem.getIsAvailable())) {
                throw new BadRequestException(menuItem.getName() + " is currently unavailable");
            }
            map.put(menuItem.getId(), menuItem);
        }

        for (OrderItemRequest item : items) {
            inventoryService.validateStock(map.get(item.getMenuItemId()), item.getQuantity());
        }
        return map;
    }

    private String generateOrderNumber() {
        String prefix = "ORD-" + LocalDate.now().toString().replace("-", "");
        int counter = 1;
        String candidate;
        do {
            candidate = prefix + "-" + String.format("%03d", counter++);
        } while (foodOrderRepository.existsByOrderNumber(candidate));
        return candidate;
    }

    private FoodOrder findEntity(Long id) {
        return foodOrderRepository.findWithItemsById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }

    private OrderResponse toSummaryResponse(FoodOrder order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerEmail(order.getCustomerEmail())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .discountAmount(order.getDiscountAmount())
                .deliveryFee(order.getDeliveryFee())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .tableNumber(order.getTableNumber())
                .deliveryAddress(order.getDeliveryAddress())
                .itemCount(order.getItems() == null ? 0 : order.getItems().size())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderResponse toDetailedResponse(FoodOrder order) {
        List<OrderItemResponse> items = order.getItems().stream().map(this::toItemResponse).toList();
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerEmail(order.getCustomerEmail())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .discountAmount(order.getDiscountAmount())
                .deliveryFee(order.getDeliveryFee())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .tableNumber(order.getTableNumber())
                .deliveryAddress(order.getDeliveryAddress())
                .itemCount(items.size())
                .items(items)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItem().getId())
                .itemNameSnapshot(item.getItemNameSnapshot())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .lineTotal(item.getLineTotal())
                .notes(item.getNotes())
                .build();
    }
}
