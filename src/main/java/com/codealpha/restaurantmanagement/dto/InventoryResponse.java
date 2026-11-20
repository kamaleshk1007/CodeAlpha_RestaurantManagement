package com.codealpha.restaurantmanagement.dto;

import com.codealpha.restaurantmanagement.enums.InventoryUnit;
import java.math.BigDecimal;

public record InventoryResponse(
        Long id,
        String ingredientName,
        BigDecimal availableQuantity,
        InventoryUnit unit) {
}
