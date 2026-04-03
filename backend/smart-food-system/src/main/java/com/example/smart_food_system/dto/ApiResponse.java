package com.example.smart_food_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private boolean success; 
    private String message;
    private Object data;
    private Object errors;

    // Convenience constructors
    public static ApiResponse ok(String message) {
        return ApiResponse.builder().success(true).message(message).data(null).errors(null).build();
    }

    public static ApiResponse ok(String message, Object data) {
        return ApiResponse.builder().success(true).message(message).data(data).errors(null).build();
    }

    public static ApiResponse fail(String message) {
        return ApiResponse.builder().success(false).message(message).data(null).errors(null).build();
    }

    public static ApiResponse fail(String message, Object errors) {
        return ApiResponse.builder().success(false).message(message).data(null).errors(errors).build();
    }
}
