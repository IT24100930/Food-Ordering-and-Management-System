package com.example.smart_food_system.repository;

import com.example.smart_food_system.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long>, JpaSpecificationExecutor<MenuItem> {
    Optional<MenuItem> findByIdAndDeletedFalse(Long id);
    Optional<MenuItem> findByItemCodeIgnoreCase(String itemCode);
    Optional<MenuItem> findByNameIgnoreCase(String name);
    boolean existsByItemCodeIgnoreCaseAndIdNot(String itemCode, Long id);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    List<MenuItem> findByDeletedFalseOrderByCategoryAscNameAsc();
}
