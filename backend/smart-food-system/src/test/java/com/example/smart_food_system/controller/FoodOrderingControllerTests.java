package com.example.smart_food_system.controller;

import com.example.smart_food_system.model.FoodOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MSSQLServer;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FoodOrderingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createMenuItem() throws Exception {
        Map<String, Object> payload = Map.of(
                "itemCode", "TST-001",
                "name", "Test Burger",
                "description", "A test item",
                "category", "Burgers",
                "price", 999,
                "costPrice", 500,
                "imageUrl", "https://example.com/burger.png",
                "isAvailable", true,
                "preparationTime", 10,
                "stockQty", 10
        );

        mockMvc.perform(post("/menu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Burger"));
    }

    @Test
    void createOrderWithMultipleItems() throws Exception {
        String payload = """
                {
                  "customerName": "QA Customer",
                  "customerPhone": "0777777777",
                  "customerEmail": "qa@example.com",
                  "orderType": "TAKEAWAY",
                  "paymentStatus": "UNPAID",
                  "discountAmount": 50,
                  "items": [
                    { "menuItemId": 1, "quantity": 2 },
                    { "menuItemId": 2, "quantity": 1 }
                  ]
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.itemCount").value(2))
                .andExpect(jsonPath("$.data.totalAmount").exists());
    }

    @Test
    void rejectInvalidOrder() throws Exception {
        String payload = """
                {
                  "customerName": "Invalid Order",
                  "customerPhone": "0700000000",
                  "orderType": "DELIVERY",
                  "items": []
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateOrderStatusWithValidTransition() throws Exception {
        Long id = createSimpleOrder();

        mockMvc.perform(patch("/orders/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", FoodOrder.OrderStatus.CONFIRMED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    void rejectInvalidStatusTransition() throws Exception {
        Long id = createSimpleOrder();

        mockMvc.perform(patch("/orders/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", FoodOrder.OrderStatus.READY))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void totalsCalculationUsesBackendValues() throws Exception {
        String payload = """
                {
                  "customerName": "Totals Test",
                  "customerPhone": "0722222222",
                  "orderType": "DELIVERY",
                  "deliveryAddress": "100 Main Street",
                  "discountAmount": 100,
                  "deliveryFee": 300,
                  "items": [
                    { "menuItemId": 1, "quantity": 1 },
                    { "menuItemId": 5, "quantity": 2 }
                  ]
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.subtotal").isNumber())
                .andExpect(jsonPath("$.data.taxAmount").isNumber())
                .andExpect(jsonPath("$.data.deliveryFee").value(300))
                .andExpect(jsonPath("$.data.discountAmount").value(100));
    }

    private Long createSimpleOrder() throws Exception {
        String createPayload = """
                {
                  "customerName": "Status Flow",
                  "customerPhone": "0712345678",
                  "orderType": "TAKEAWAY",
                  "items": [
                    { "menuItemId": 1, "quantity": 1 }
                  ]
                }
                """;

        String response = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asLong();
    }
}
