package com.codealpha.restaurantmanagement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull @Positive Long menuItemId,
        @NotNull @Positive Integer quantity) {
}
