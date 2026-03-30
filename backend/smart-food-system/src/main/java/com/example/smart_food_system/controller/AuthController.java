package com.example.smart_food_system.controller;

import com.example.smart_food_system.dto.*;
import com.example.smart_food_system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth") 
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    private final UserService userService;

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest req) {
        Map<String, Object> response = new HashMap<>();
        try {
            UserResponse user = userService.login(req);
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("role", user.getRole());
            response.put("name", user.getFirstName() + " " + user.getLastName());
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(401).body(response);
        }
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest req) {
        try {
            userService.register(req);
            return ResponseEntity.status(201).body(ApiResponse.ok("Registration successful! Please login."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    // POST /api/auth/forgot-password
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = userService.forgotPassword(req.getEmail());
            response.put("success", true);
            response.put("message", "Reset token generated successfully.");
            response.put("resetToken", token); 
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // PUT /api/auth/reset-password
    @PutMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            userService.resetPassword(req);
            return ResponseEntity.ok(ApiResponse.ok("Password reset successfully! Please login."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }
}