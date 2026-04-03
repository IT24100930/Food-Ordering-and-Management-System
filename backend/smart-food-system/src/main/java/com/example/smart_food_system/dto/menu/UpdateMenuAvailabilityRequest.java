package com.example.smart_food_system.dto.menu;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMenuAvailabilityRequest {
    @NotNull
    private Boolean isAvailable;
}
