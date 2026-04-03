package com.example.smart_food_system.config;

import com.example.smart_food_system.model.FoodOrder;
import com.example.smart_food_system.model.MenuItem;
import com.example.smart_food_system.model.OrderItem;
import com.example.smart_food_system.model.User;
import com.example.smart_food_system.repository.FoodOrderRepository;
import com.example.smart_food_system.repository.MenuItemRepository;
import com.example.smart_food_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AtomicInteger demoOrderCounter = new AtomicInteger(1);

    @Override
    public void run(String... args) {
        seedUsers();
        seedMenuItems();
        seedOrders();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            log.info("Users already exist, skipping user seed.");
            return;
        }

        log.info("Seeding default users...");
        userRepository.save(User.builder()
                .firstName("System").lastName("Admin")
                .email("admin@urbanplate.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(User.Role.ADMIN)
                .address("123 Admin Street, Colombo")
                .telephone("0112345678")
                .status(User.Status.ACTIVE)
                .build());

        userRepository.save(User.builder()
                .firstName("John").lastName("Staff")
                .email("staff@urbanplate.com")
                .password(passwordEncoder.encode("Staff@123"))
                .role(User.Role.STAFF)
                .address("456 Staff Lane, Colombo")
                .telephone("0119876543")
                .status(User.Status.ACTIVE)
                .build());

        userRepository.save(User.builder()
                .firstName("Jane").lastName("Customer")
                .email("customer@example.com")
                .password(passwordEncoder.encode("Customer@123"))
                .role(User.Role.CUSTOMER)
                .address("789 Customer Road, Colombo")
                .telephone("0771234567")
                .status(User.Status.ACTIVE)
                .build());
    }

    private void seedMenuItems() {
        if (menuItemRepository.count() > 0) {
            log.info("Menu items already exist, skipping menu seed.");
            return;
        }

        log.info("Seeding menu items...");
        menuItemRepository.saveAll(List.of(
                menu("BRG-001", "Classic Beef Burger", "Juicy grilled beef patty with cheese and house sauce.", "Burgers", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd", 1250, 700, true, 15, 30),
                menu("BRG-002", "Crispy Chicken Burger", "Golden fried chicken, slaw, pickles and spicy mayo.", "Burgers", "https://images.unsplash.com/photo-1550547660-d9450f859349", 1180, 650, true, 14, 25),
                menu("RIC-001", "Chicken Fried Rice", "Wok-tossed rice with chicken, vegetables and egg.", "Rice", "https://images.unsplash.com/photo-1512058564366-18510be2db19", 980, 540, true, 12, 40),
                menu("RIC-002", "Seafood Nasi Goreng", "Spiced Indonesian-style fried rice with prawns and squid.", "Rice", "https://images.unsplash.com/photo-1603133872878-684f208fb84b", 1450, 860, true, 18, 18),
                menu("DRK-001", "Fresh Lime Soda", "Sparkling fresh lime with mint and a touch of sugar.", "Drinks", "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd", 320, 110, true, 4, 80),
                menu("DRK-002", "Iced Mocha", "Cold mocha with espresso, chocolate and cream.", "Drinks", "https://images.unsplash.com/photo-1461023058943-07fcbe16d735", 480, 190, true, 6, 60),
                menu("DES-001", "Chocolate Lava Cake", "Warm chocolate cake with a molten center.", "Desserts", "https://images.unsplash.com/photo-1614707267537-b85aaf00c4b7", 690, 260, true, 10, 22),
                menu("DES-002", "Vanilla Cheesecake", "Creamy cheesecake with berry compote.", "Desserts", "https://images.unsplash.com/photo-1533134242443-d4fd215305ad", 720, 280, true, 8, 20),
                menu("KOT-001", "Chicken Kottu", "Sri Lankan chopped roti with chicken and vegetables.", "Kottu", "https://images.unsplash.com/photo-1504674900247-0877df9cc836", 1100, 620, true, 16, 35),
                menu("KOT-002", "Cheese Kottu", "Loaded kottu with melted cheese and chili flakes.", "Kottu", "https://images.unsplash.com/photo-1544025162-d76694265947", 1280, 740, true, 17, 15),
                menu("PIZ-001", "Margherita Pizza", "Tomato, mozzarella and basil on a thin crust.", "Pizza", "https://images.unsplash.com/photo-1548365328-9f547fb0953b", 1650, 890, true, 20, 16),
                menu("PIZ-002", "Pepperoni Pizza", "Pepperoni, mozzarella and oregano.", "Pizza", "https://images.unsplash.com/photo-1513104890138-7c749659a591", 1890, 1020, true, 22, 14),
                menu("PIZ-003", "Veggie Supreme Pizza", "Bell peppers, onion, olives, mushroom and corn.", "Pizza", "https://images.unsplash.com/photo-1511689660979-10d2b1aada49", 1740, 930, false, 21, 12),
                menu("BRG-003", "BBQ Bacon Burger", "Beef burger with smoky BBQ glaze and bacon.", "Burgers", "https://images.unsplash.com/photo-1572802419224-296b0aeee0d9", 1480, 840, true, 18, 20),
                menu("RIC-003", "Veg Curry Rice Bowl", "Rice bowl with seasonal curries and sambol.", "Rice", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd", 890, 420, true, 11, 24)
        ));
    }

    private void seedOrders() {
        if (foodOrderRepository.count() > 0) {
            log.info("Orders already exist, skipping order seed.");
            return;
        }

        List<MenuItem> menu = menuItemRepository.findByDeletedFalseOrderByCategoryAscNameAsc();
        if (menu.size() < 6) {
            return;
        }

        log.info("Seeding demo orders...");
        foodOrderRepository.saveAll(List.of(
                seedOrder("Nimal Perera", "0771234567", "nimal@example.com", FoodOrder.OrderType.TAKEAWAY, FoodOrder.OrderStatus.PENDING, FoodOrder.PaymentStatus.UNPAID, null, null, "Extra ketchup", menu.get(0), 2, menu.get(4), 1),
                seedOrder("Sahan Fernando", "0710000000", "sahan@example.com", FoodOrder.OrderType.DELIVERY, FoodOrder.OrderStatus.PREPARING, FoodOrder.PaymentStatus.PAID, null, "No. 12, Lake Road, Colombo 08", "Call on arrival", menu.get(2), 1, menu.get(8), 1),
                seedOrder("Asha Silva", "0765555555", "asha@example.com", FoodOrder.OrderType.DINE_IN, FoodOrder.OrderStatus.COMPLETED, FoodOrder.PaymentStatus.PAID, "T12", null, null, menu.get(10), 1, menu.get(6), 2),
                seedOrder("Ravi Senanayake", "0759999999", null, FoodOrder.OrderType.TAKEAWAY, FoodOrder.OrderStatus.CANCELLED, FoodOrder.PaymentStatus.REFUNDED, null, null, "Customer cancelled", menu.get(1), 1, menu.get(5), 2)
        ));
    }

    private MenuItem menu(String code, String name, String description, String category, String imageUrl,
                          int price, int costPrice, boolean available, int preparationTime, int stockQty) {
        return MenuItem.builder()
                .itemCode(code)
                .name(name)
                .description(description)
                .category(category)
                .price(BigDecimal.valueOf(price))
                .costPrice(BigDecimal.valueOf(costPrice))
                .imageUrl(imageUrl)
                .isAvailable(available)
                .preparationTime(preparationTime)
                .stockQty(stockQty)
                .deleted(false)
                .build();
    }

    private FoodOrder seedOrder(String customerName, String phone, String email,
                                FoodOrder.OrderType type, FoodOrder.OrderStatus status, FoodOrder.PaymentStatus paymentStatus,
                                String tableNumber, String deliveryAddress, String notes,
                                MenuItem firstItem, int firstQty, MenuItem secondItem, int secondQty) {
        FoodOrder order = FoodOrder.builder()
                .orderNumber("DEMO-" + String.format("%03d", demoOrderCounter.getAndIncrement()))
                .customerName(customerName)
                .customerPhone(phone)
                .customerEmail(email)
                .orderType(type)
                .status(status)
                .paymentStatus(paymentStatus)
                .notes(notes)
                .tableNumber(tableNumber)
                .deliveryAddress(deliveryAddress)
                .build();

        OrderItem item1 = orderItem(order, firstItem, firstQty);
        OrderItem item2 = orderItem(order, secondItem, secondQty);
        order.setItems(new ArrayList<>(List.of(item1, item2)));

        BigDecimal subtotal = item1.getLineTotal().add(item2.getLineTotal()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = subtotal.multiply(BigDecimal.valueOf(0.10)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal deliveryFee = type == FoodOrder.OrderType.DELIVERY
                ? BigDecimal.valueOf(250).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setDiscountAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(subtotal.add(taxAmount).add(deliveryFee));
        return order;
    }

    private OrderItem orderItem(FoodOrder order, MenuItem menuItem, int quantity) {
        return OrderItem.builder()
                .order(order)
                .menuItem(menuItem)
                .itemNameSnapshot(menuItem.getName())
                .unitPrice(menuItem.getPrice())
                .quantity(quantity)
                .lineTotal(menuItem.getPrice().multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP))
                .build();
    }
}
