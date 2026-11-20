package com.codealpha.restaurantmanagement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateMenuItemIngredientRequest(
        @NotNull @Positive Long menuItemId,
        @NotNull @Positive Long inventoryId,
        @NotNull @DecimalMin("0.001") @Digits(integer = 16, fraction = 3) BigDecimal requiredQuantity) {
}
