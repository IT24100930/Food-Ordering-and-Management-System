package com.urbanplate.billing.controller;

import com.urbanplate.billing.model.Discount;
import com.urbanplate.billing.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DiscountController {

    private final DiscountRepository discountRepository;

    // GET all discounts
    @GetMapping
    public ResponseEntity<List<Discount>> getAllDiscounts() {
        List<Discount> discounts = discountRepository.findAll();
        return ResponseEntity.ok(discounts);
    }

    // GET discount by ID
    @GetMapping("/{id}")
    public ResponseEntity<Discount> getDiscountById(@PathVariable Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found with id: " + id));
        return ResponseEntity.ok(discount);
    }

    // GET discount by coupon code
    @GetMapping("/code/{couponCode}")
    public ResponseEntity<Discount> getDiscountByCode(@PathVariable String couponCode) {
        Discount discount = discountRepository.findByCouponCode(couponCode.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Discount not found with code: " + couponCode));
        return ResponseEntity.ok(discount);
    }

    // GET active discounts only
    @GetMapping("/active")
    public ResponseEntity<List<Discount>> getActiveDiscounts() {
        List<Discount> discounts = discountRepository.findByActiveTrue();
        return ResponseEntity.ok(discounts);
    }

    // GET valid discounts for current date
    @GetMapping("/valid")
    public ResponseEntity<List<Discount>> getValidDiscounts() {
        LocalDate today = LocalDate.now();
        List<Discount> discounts = discountRepository.findByValidFromBeforeAndValidUntilAfter(today, today);
        return ResponseEntity.ok(discounts);
    }

    // CREATE new discount
    @PostMapping
    public ResponseEntity<Discount> createDiscount(@RequestBody Discount discount) {
        // Set default values if not provided
        if (discount.getValidFrom() == null) {
            discount.setValidFrom(LocalDate.now());
        }
        if (discount.getValidUntil() == null) {
            discount.setValidUntil(LocalDate.now().plusMonths(1));
        }
        if (discount.getMinimumOrderAmount() == null) {
            discount.setMinimumOrderAmount(java.math.BigDecimal.ZERO);
        }
        if (discount.getUsageCount() == null) {
            discount.setUsageCount(0);
        }
        if (discount.getActive() == null) {
            discount.setActive(true);
        }

        // Convert coupon code to uppercase
        discount.setCouponCode(discount.getCouponCode().toUpperCase());

        Discount savedDiscount = discountRepository.save(discount);
        return new ResponseEntity<>(savedDiscount, HttpStatus.CREATED);
    }

    // UPDATE discount
    @PutMapping("/{id}")
    public ResponseEntity<Discount> updateDiscount(@PathVariable Long id, @RequestBody Discount discountDetails) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found with id: " + id));

        discount.setCouponCode(discountDetails.getCouponCode().toUpperCase());
        discount.setType(discountDetails.getType());
        discount.setValue(discountDetails.getValue());
        discount.setDescription(discountDetails.getDescription());
        discount.setValidFrom(discountDetails.getValidFrom());
        discount.setValidUntil(discountDetails.getValidUntil());
        discount.setUsageLimit(discountDetails.getUsageLimit());
        discount.setMinimumOrderAmount(discountDetails.getMinimumOrderAmount());
        discount.setActive(discountDetails.getActive());

        Discount updatedDiscount = discountRepository.save(discount);
        return ResponseEntity.ok(updatedDiscount);
    }

    // DELETE discount (soft delete - deactivate)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiscount(@PathVariable Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found with id: " + id));
        discount.setActive(false);
        discountRepository.save(discount);
        return ResponseEntity.noContent().build();
    }

    // HARD DELETE (permanently remove) - optional
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentDeleteDiscount(@PathVariable Long id) {
        discountRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Increment usage count when discount is applied
    @PostMapping("/{id}/increment-usage")
    public ResponseEntity<Discount> incrementUsageCount(@PathVariable Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found with id: " + id));

        discount.setUsageCount(discount.getUsageCount() + 1);
        Discount updatedDiscount = discountRepository.save(discount);
        return ResponseEntity.ok(updatedDiscount);
    }
}