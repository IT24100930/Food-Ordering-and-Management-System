package com.urbanplate.billing.repository;

import com.urbanplate.billing.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    // Find discount by coupon code
    Optional<Discount> findByCouponCode(String couponCode);

    // Find all active discounts
    List<Discount> findByActiveTrue();

    // Find discounts valid for a specific date
    List<Discount> findByValidFromBeforeAndValidUntilAfter(LocalDate date, LocalDate date2);

    // Find discounts by type
    List<Discount> findByType(Discount.DiscountType type);
}