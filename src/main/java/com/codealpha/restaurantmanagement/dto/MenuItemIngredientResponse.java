package com.codealpha.restaurantmanagement.dto;

import com.codealpha.restaurantmanagement.enums.InventoryUnit;
import java.math.BigDecimal;

public record MenuItemIngredientResponse(
        Long id,
        Long menuItemId,
        Long inventoryId,
        String ingredientName,
        InventoryUnit unit,
        BigDecimal requiredQuantity) {
}
