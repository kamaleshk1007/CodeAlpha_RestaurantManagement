package com.codealpha.restaurantmanagement.dto;

import com.codealpha.restaurantmanagement.enums.InventoryUnit;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateInventoryRequest(
        @NotBlank @Size(max = 120) String ingredientName,
        @NotNull @PositiveOrZero @Digits(integer = 16, fraction = 3) BigDecimal availableQuantity,
        @NotNull InventoryUnit unit) {
}
