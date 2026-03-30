package com.example.smart_food_system.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String role;
    private String status;
    private String address;
    private String telephone;
} 