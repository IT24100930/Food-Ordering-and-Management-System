package com.example.smart_food_system.repository;

import com.example.smart_food_system.model.FoodOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long>, JpaSpecificationExecutor<FoodOrder> {
    boolean existsByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = {"items", "items.menuItem"})
    Optional<FoodOrder> findWithItemsById(Long id);
}
