package com.example.smart_food_system.controller;

import com.example.smart_food_system.dto.*;
import com.example.smart_food_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor 
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    private final UserService userService;

    // GET /api/users — get all users
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            List<UserResponse> users = userService.getAllUsers();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "users", users,
                "total", users.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // GET /api/users/stats — counts per role
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            Map<String, Long> stats = userService.getStats();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "totalUsers",     stats.get("totalUsers"),
                "totalAdmins",    stats.get("totalAdmins"),
                "totalStaff",     stats.get("totalStaff"),
                "totalCustomers", stats.get("totalCustomers")
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // GET /api/users/{id} — get one user
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = userService.getUserById(id);
            return ResponseEntity.ok(Map.of("success", true, "user", user));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // POST /api/users — create user (admin)
    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@RequestBody RegisterRequest req) {
        try {
            userService.createUser(req);
            return ResponseEntity.status(201).body(ApiResponse.ok("User created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    // PUT /api/users/{id} — update user
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long id,
                                                   @RequestBody UpdateUserRequest req) {
        try {
            userService.updateUser(id, req);
            return ResponseEntity.ok(ApiResponse.ok("User updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    // DELETE /api/users/{id} — soft delete (deactivate)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.ok("User deactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }
}