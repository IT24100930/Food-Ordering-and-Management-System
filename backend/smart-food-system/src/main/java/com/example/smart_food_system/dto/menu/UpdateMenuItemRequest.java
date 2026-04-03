package com.example.smart_food_system.dto.menu;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateMenuItemRequest {
    @NotBlank
    @Size(max = 40)
    private String itemCode;

    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotBlank
    @Size(max = 60)
    private String category;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal costPrice;

    @Size(max = 500)
    private String imageUrl;

    private Boolean isAvailable;

    @Min(0)
    private Integer preparationTime;

    @Min(0)
    private Integer stockQty;
}
