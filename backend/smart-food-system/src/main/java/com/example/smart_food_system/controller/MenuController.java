package com.example.smart_food_system.controller;

import com.example.smart_food_system.dto.ApiResponse;
import com.example.smart_food_system.dto.menu.CreateMenuItemRequest;
import com.example.smart_food_system.dto.menu.UpdateMenuAvailabilityRequest;
import com.example.smart_food_system.dto.menu.UpdateMenuItemRequest;
import com.example.smart_food_system.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<ApiResponse> getMenu(@RequestParam(required = false) String query,
                                               @RequestParam(required = false) String category,
                                               @RequestParam(required = false) Boolean available,
                                               @RequestParam(required = false) Double minPrice,
                                               @RequestParam(required = false) Double maxPrice) {
        return ResponseEntity.ok(ApiResponse.ok("Menu fetched successfully",
                menuService.getAll(query, category, available, minPrice, maxPrice)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Menu item fetched successfully", menuService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody CreateMenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Menu item created successfully", menuService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateMenuItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Menu item updated successfully", menuService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        menuService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.ok("Menu item deleted successfully"));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<ApiResponse> updateAvailability(@PathVariable Long id,
                                                          @Valid @RequestBody UpdateMenuAvailabilityRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Menu item availability updated successfully",
                menuService.updateAvailability(id, request)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> search(@RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.ok("Menu search completed", menuService.getAll(query, null, null, null, null)));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse> filter(@RequestParam(required = false) String category,
                                              @RequestParam(required = false) Boolean available,
                                              @RequestParam(required = false) Double minPrice,
                                              @RequestParam(required = false) Double maxPrice) {
        return ResponseEntity.ok(ApiResponse.ok("Menu filter completed",
                menuService.getAll(null, category, available, minPrice, maxPrice)));
    }
}
