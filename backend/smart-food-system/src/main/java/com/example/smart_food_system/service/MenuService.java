package com.example.smart_food_system.service;

import com.example.smart_food_system.dto.menu.CreateMenuItemRequest;
import com.example.smart_food_system.dto.menu.MenuItemResponse;
import com.example.smart_food_system.dto.menu.UpdateMenuAvailabilityRequest;
import com.example.smart_food_system.dto.menu.UpdateMenuItemRequest;
import com.example.smart_food_system.exception.DuplicateResourceException;
import com.example.smart_food_system.exception.NotFoundException;
import com.example.smart_food_system.model.MenuItem;
import com.example.smart_food_system.repository.MenuItemRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuItemRepository menuItemRepository;

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getAll(String query, String category, Boolean available, Double minPrice, Double maxPrice) {
        Specification<MenuItem> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (query != null && !query.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%"));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }
            if (available != null) {
                predicates.add(cb.equal(root.get("isAvailable"), available));
            }
            if (minPrice != null) {
                predicates.add(cb.ge(root.get("price"), BigDecimal.valueOf(minPrice)));
            }
            if (maxPrice != null) {
                predicates.add(cb.le(root.get("price"), BigDecimal.valueOf(maxPrice)));
            }

            cq.orderBy(cb.asc(root.get("category")), cb.asc(root.get("name")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return menuItemRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional
    public MenuItemResponse create(CreateMenuItemRequest request) {
        validateUniqueness(request.getItemCode(), request.getName(), null);
        MenuItem item = new MenuItem();
        apply(item, request.getItemCode(), request.getName(), request.getDescription(), request.getCategory(),
                request.getPrice(), request.getCostPrice(), request.getImageUrl(), request.getIsAvailable(),
                request.getPreparationTime(), request.getStockQty());
        MenuItem saved = menuItemRepository.save(item);
        log.info("Created menu item {} ({})", saved.getName(), saved.getItemCode());
        return toResponse(saved);
    }

    @Transactional
    public MenuItemResponse update(Long id, UpdateMenuItemRequest request) {
        MenuItem item = findEntity(id);
        validateUniqueness(request.getItemCode(), request.getName(), id);
        apply(item, request.getItemCode(), request.getName(), request.getDescription(), request.getCategory(),
                request.getPrice(), request.getCostPrice(), request.getImageUrl(), request.getIsAvailable(),
                request.getPreparationTime(), request.getStockQty());
        return toResponse(menuItemRepository.save(item));
    }

    @Transactional
    public MenuItemResponse updateAvailability(Long id, UpdateMenuAvailabilityRequest request) {
        MenuItem item = findEntity(id);
        item.setIsAvailable(request.getIsAvailable());
        return toResponse(menuItemRepository.save(item));
    }

    @Transactional
    public void softDelete(Long id) {
        MenuItem item = findEntity(id);
        item.setDeleted(true);
        item.setIsAvailable(false);
        menuItemRepository.save(item);
    }

    public MenuItem findEntity(Long id) {
        return menuItemRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Menu item not found"));
    }

    private void validateUniqueness(String itemCode, String name, Long id) {
        if (id == null) {
            if (menuItemRepository.findByItemCodeIgnoreCase(itemCode).filter(item -> !item.getDeleted()).isPresent()) {
                throw new DuplicateResourceException("Menu item code already exists");
            }
            if (menuItemRepository.findByNameIgnoreCase(name).filter(item -> !item.getDeleted()).isPresent()) {
                throw new DuplicateResourceException("Menu item name already exists");
            }
            return;
        }
        if (menuItemRepository.existsByItemCodeIgnoreCaseAndIdNot(itemCode, id)) {
            throw new DuplicateResourceException("Menu item code already exists");
        }
        if (menuItemRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("Menu item name already exists");
        }
    }

    private void apply(MenuItem item, String itemCode, String name, String description, String category,
                       BigDecimal price, BigDecimal costPrice, String imageUrl, Boolean isAvailable,
                       Integer preparationTime, Integer stockQty) {
        item.setItemCode(itemCode.trim());
        item.setName(name.trim());
        item.setDescription(description);
        item.setCategory(category.trim());
        item.setPrice(price);
        item.setCostPrice(costPrice);
        item.setImageUrl(imageUrl);
        item.setIsAvailable(isAvailable == null ? Boolean.TRUE : isAvailable);
        item.setPreparationTime(preparationTime == null ? 0 : preparationTime);
        item.setStockQty(stockQty == null ? 0 : stockQty);
        item.setDeleted(false);
    }

    private MenuItemResponse toResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .itemCode(item.getItemCode())
                .name(item.getName())
                .description(item.getDescription())
                .category(item.getCategory())
                .price(item.getPrice())
                .costPrice(item.getCostPrice())
                .imageUrl(item.getImageUrl())
                .isAvailable(item.getIsAvailable())
                .preparationTime(item.getPreparationTime())
                .stockQty(item.getStockQty())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
