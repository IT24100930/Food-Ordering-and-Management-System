package com.example.smart_food_system.dto;

import com.example.smart_food_system.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id; 
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String address;
    private String telephone;
    private String status;
    private LocalDateTime createdAt;

    // Convert User entity → UserResponse (hides password)
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .address(user.getAddress())
                .telephone(user.getTelephone())
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}