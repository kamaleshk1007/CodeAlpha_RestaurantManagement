package com.codealpha.restaurantmanagement.service;

import com.codealpha.restaurantmanagement.dto.CreateInventoryRequest;
import com.codealpha.restaurantmanagement.dto.InventoryResponse;
import com.codealpha.restaurantmanagement.dto.UpdateInventoryRequest;
import com.codealpha.restaurantmanagement.entity.Inventory;
import com.codealpha.restaurantmanagement.exception.DuplicateResourceException;
import com.codealpha.restaurantmanagement.exception.ResourceNotFoundException;
import com.codealpha.restaurantmanagement.repository.InventoryRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public InventoryResponse createInventoryItem(CreateInventoryRequest request) {
        String ingredientName = request.ingredientName().trim();
        if (inventoryRepository.existsByIngredientNameIgnoreCase(ingredientName)) {
            throw new DuplicateResourceException("An inventory item named '" + ingredientName + "' already exists");
        }
        validateNonNegativeQuantity(request.availableQuantity());

        Inventory inventory = new Inventory();
        inventory.setIngredientName(ingredientName);
        inventory.setAvailableQuantity(request.availableQuantity());
        inventory.setUnit(request.unit());
        return toResponse(inventoryRepository.save(inventory));
    }

    public List<InventoryResponse> getAllInventoryItems() {
        return inventoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    public InventoryResponse getInventoryItemById(Long id) {
        return toResponse(findInventory(id));
    }

    @Transactional
    public InventoryResponse updateInventoryItem(Long id, UpdateInventoryRequest request) {
        validateNonNegativeQuantity(request.availableQuantity());
        Inventory inventory = findInventory(id);
        String ingredientName = request.ingredientName().trim();
        if (!inventory.getIngredientName().equalsIgnoreCase(ingredientName)
                && inventoryRepository.existsByIngredientNameIgnoreCase(ingredientName)) {
            throw new DuplicateResourceException("An inventory item named '" + ingredientName + "' already exists");
        }

        inventory.setIngredientName(ingredientName);
        inventory.setAvailableQuantity(request.availableQuantity());
        return toResponse(inventory);
    }

    private void validateNonNegativeQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.signum() < 0) {
            throw new IllegalArgumentException("Inventory quantity must not be negative");
        }
    }

    private Inventory findInventory(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item", id));
    }

    private InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(inventory.getId(), inventory.getIngredientName(),
                inventory.getAvailableQuantity(), inventory.getUnit());
    }
}
