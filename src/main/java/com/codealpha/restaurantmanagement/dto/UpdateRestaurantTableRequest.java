package com.codealpha.restaurantmanagement.dto;

import com.codealpha.restaurantmanagement.enums.TableStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateRestaurantTableRequest(
        @NotNull @Positive Integer tableNumber,
        @NotNull @Positive Integer seatingCapacity,
        @NotNull TableStatus status) {
}
